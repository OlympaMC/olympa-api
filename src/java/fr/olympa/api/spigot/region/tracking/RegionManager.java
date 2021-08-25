package fr.olympa.api.spigot.region.tracking;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.google.common.collect.Sets;

import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.spigot.customevents.AsyncPlayerMoveRegionsEvent;
import fr.olympa.api.spigot.customevents.WorldTrackingEvent;
import fr.olympa.api.spigot.region.Region;
import fr.olympa.api.spigot.region.shapes.WorldRegion;
import fr.olympa.api.spigot.region.tracking.RegionEvent.EntryEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.ExitEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.RegionEventReason;
import fr.olympa.api.spigot.region.tracking.flags.*;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import io.papermc.paper.event.block.BlockPreDispenseEvent;

public class RegionManager implements Listener, ModuleApi<OlympaCore> {

	/**
	 * Compare les priorités des régions de la plus faible à la plus forte
	 */
	public static final Comparator<TrackedRegion> REGION_COMPARATOR = (o1, o2) -> Integer.compare(o1.getPriority().getSlot(), o2.getPriority().getSlot());

	/**
	 * Compare les priorités des régions de la plus forte à la plus faible
	 */
	public static final Comparator<TrackedRegion> REGION_COMPARATOR_INVERT = (o1, o2) -> Integer.compare(o2.getPriority().getSlot(), o1.getPriority().getSlot());

	private Map<String, TrackedRegion> trackedRegions;
	private Map<World, TrackedRegion> worldRegions;
	private Map<String, Consumer<WorldTrackingEvent>> trackAwait;
	private Map<Player, Set<TrackedRegion>> inRegions;

	private boolean enable = false;

	public TrackedRegion registerRegion(Region region, String id, EventPriority priority, Flag... flags) {
		Validate.notNull(region, id + " is a null region");
		Validate.isTrue(!id.endsWith("_global"), "The region ID cannot end with _global");
		Validate.isTrue(!trackedRegions.containsKey(id), "A region with ID " + id + " already exists");
		TrackedRegion tracked = new TrackedRegion(region, id, priority, flags);
		trackedRegions.put(id, tracked);
		for (Player p : Bukkit.getOnlinePlayers())
			if (region.isIn(p)) {
				Set<TrackedRegion> regionsSet = null;
				EntryEvent event = null;
				for (Flag flag : flags) {
					if (regionsSet == null) {
						regionsSet = getApplicableRegions(p.getLocation());
						event = new EntryEvent(p, regionsSet, RegionEventReason.REGION_CREATION);
					}
					if (flag.enters(event) == ActionResult.DENY)
						OlympaCore.getInstance().getLogger().warning("Entry cancelled when new region - ignored.");
				}
			}
		return tracked;
	}

	public void unregisterRegion(String id) {
		if (enable) trackedRegions.remove(id);
	}

	private void registerWorld(World world) {
		if (worldRegions.containsKey(world))
			return;
		TrackedRegion region = new TrackedRegion(new WorldRegion(world), world.getName() + "_global", EventPriority.LOWEST);
		worldRegions.put(world, region);
		trackedRegions.put(region.getID(), region);
		OlympaCore.getInstance().sendMessage("Région globale enregistrée pour le monde §e" + world.getName());
		WorldTrackingEvent event = new WorldTrackingEvent(world, region);
		Bukkit.getPluginManager().callEvent(event);
		Consumer<WorldTrackingEvent> consumer = trackAwait.remove(world.getName());
		if (consumer != null)
			consumer.accept(event);
	}

	private void unregisterWorld(World world) {
		TrackedRegion region = worldRegions.remove(world);
		if (region == null)
			return;
		unregisterRegion(region.getID());
		OlympaCore.getInstance().sendMessage("Région globale supprimée pour le monde §e" + world.getName());
	}

	public void awaitWorldTracking(String worldName, Consumer<WorldTrackingEvent> consumer) {
		World world = Bukkit.getWorld(worldName);
		TrackedRegion region = worldRegions.get(world);
		if (region != null) {
			consumer.accept(new WorldTrackingEvent(world, region));
		}else trackAwait.put(worldName, consumer);
	}

	public boolean isIn(Player p, String id) {
		Set<TrackedRegion> regions = inRegions.get(p);
		return regions.stream().anyMatch(x -> x.getID().equals(id));
	}

	public TrackedRegion getWorldRegion(World world) {
		return worldRegions.get(world);
	}

	public Map<String, TrackedRegion> getTrackedRegions() {
		return trackedRegions;
	}

	public Set<TrackedRegion> getCachedPlayerRegions(Player p) {
		return inRegions.getOrDefault(p, Collections.EMPTY_SET);
	}

	public Set<TrackedRegion> getApplicableRegions(Location location) {
		return trackedRegions.values().stream().filter(x -> x.getRegion().isIn(location)).collect(Collectors.toSet());
	}

	public <T extends Flag> void fireEvent(Location location, Class<T> flagClass, Consumer<T> consumer) {
		trackedRegions.values().stream().filter(x -> x.getRegion().isIn(location)).sorted(REGION_COMPARATOR).forEach(x -> {
			T flag = x.getFlag(flagClass);
			if (flag != null)
				consumer.accept(flag);
		});
	}

	public TrackedRegion getMostImportantRegion(Location location) {
		return trackedRegions.values().stream().filter(x -> x.getRegion().isIn(location)).sorted(REGION_COMPARATOR_INVERT).findFirst().get();
	}

	public <T extends Flag> T getMostImportantFlag(Location location, Class<T> flagClass) {
		return trackedRegions.values().stream().filter(x -> x.getFlag(flagClass) != null && x.getRegion().isIn(location)).sorted(REGION_COMPARATOR_INVERT).findFirst().map(region -> region.getFlag(flagClass)).orElse(null);
	}

	protected void updateRegion(TrackedRegion region, Region from, Region to) {
		for (Player p : Bukkit.getOnlinePlayers()) {
			boolean wasIn = from.isIn(p);
			boolean isIn = to.isIn(p);
			if (wasIn == isIn)
				continue;
			if (wasIn) {
				Set<TrackedRegion> regionsSet = getCachedPlayerRegions(p);
				regionsSet.remove(region);
				ExitEvent event = new ExitEvent(p, regionsSet, RegionEventReason.REGION_UPDATE);
				for (Flag flag : region.getFlags())
					if (flag.leaves(event) == ActionResult.DENY)
						OlympaCore.getInstance().getLogger().warning("Leave cancelled when region editing - ignored.");
			} else { // isIn
				Set<TrackedRegion> regionsSet = getCachedPlayerRegions(p);
				regionsSet.add(region);
				EntryEvent event = new EntryEvent(p, regionsSet, RegionEventReason.REGION_UPDATE);
				for (Flag flag : region.getFlags())
					if (flag.enters(event) == ActionResult.DENY)
						OlympaCore.getInstance().getLogger().warning("Entry cancelled when region editing - ignored.");
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onWorldLoad(WorldInitEvent e) {
		registerWorld(e.getWorld());
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e) {
		unregisterWorld(e.getWorld());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		Set<TrackedRegion> regions = getApplicableRegions(p.getLocation());
		inRegions.put(p, regions);
		
		EntryEvent event = new EntryEvent(p, regions, RegionEventReason.JOIN);
		for (TrackedRegion enter : regions)
			try {
				for (Flag flag : enter.getFlags())
					if (flag.enters(event) == ActionResult.DENY)
						OlympaCore.getInstance().getLogger().warning("Entry cancelled on join - ignored.");
			} catch (Exception ex) {
				ex.printStackTrace();
			}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (trackedRegions.isEmpty())
			return;
		if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo()))
			return;

		Player player = e.getPlayer();

		Set<TrackedRegion> lastRegions = inRegions.get(player);
		Set<TrackedRegion> lastRegionsSave = Set.copyOf(lastRegions);
		boolean madeChange = false;

		List<Location> locations = e.getFrom().getWorld() == e.getTo().getWorld() ? SpigotUtils.getLocationsBetween(e.getFrom(), e.getTo(), true) : Arrays.asList(e.getTo());

		Location old = e.getFrom();
		for (Location location : locations) {
			Set<TrackedRegion> applicable = getApplicableRegions(location);

			Set<TrackedRegion> entered = Sets.difference(applicable, lastRegions);
			Set<TrackedRegion> exited = Sets.difference(lastRegions, applicable);

			if (!entered.isEmpty() || !exited.isEmpty()) {
				madeChange = true;

				ActionResult result = ActionResult.ALLOW;

				ExitEvent exitEvent = new ExitEvent(player, applicable, RegionEventReason.MOVE);
				for (TrackedRegion exit : exited)
					try {
						for (Flag flag : exit.getFlags())
							result = result.or(flag.leaves(exitEvent));
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				EntryEvent entryEvent = new EntryEvent(player, applicable, RegionEventReason.MOVE);
				for (TrackedRegion enter : entered)
					try {
						for (Flag flag : enter.getFlags())
							result = result.or(flag.enters(entryEvent));
					} catch (Exception ex) {
						ex.printStackTrace();
					}

				// TODO if DENY mid-way, re-execute back
				if (result == ActionResult.DENY)
					break;
				if (result == ActionResult.TELEPORT_ELSEWHERE)
					return;

				lastRegions = applicable;
			}
			old = location;
		}
		if (old != e.getFrom() && madeChange) {
			inRegions.put(player, lastRegions);
			final Set<TrackedRegion> regions = lastRegions;
			Bukkit.getScheduler().runTaskAsynchronously(OlympaCore.getInstance(), () -> Bukkit.getPluginManager().callEvent(new AsyncPlayerMoveRegionsEvent(player, regions, lastRegionsSave)));
		}
	}

	@EventHandler
	public void onBreakBlock(BlockBreakEvent e) {
		fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onPlaceBlock(BlockPlaceEvent e) {
		fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onFertilize(BlockFertilizeEvent e) {
		fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onIgnite(BlockIgniteEvent e) {
		fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketEmptyEvent e) {
		fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onBucketEmpty(PlayerBucketFillEvent e) {
		fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getBlock()));
	}

	@EventHandler
	public void onEntityInteract(PlayerInteractEntityEvent e) {
		if (e.getRightClicked() instanceof ItemFrame || e.getRightClicked() instanceof ArmorStand)
			fireEvent(e.getRightClicked().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, e.getPlayer(), e.getRightClicked()));
	}

	@EventHandler
	public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
		fireEvent(e.getRightClicked().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, e.getPlayer(), e.getRightClicked()));
	}

	@EventHandler
	public void onHangingPlace(HangingPlaceEvent e) {
		fireEvent(e.getEntity().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, e.getPlayer(), e.getEntity()));
	}

	@EventHandler
	public void onHangingBreak(HangingBreakEvent e) {
		if (e instanceof HangingBreakByEntityEvent)
			return;
		fireEvent(e.getEntity().getLocation(), PhysicsFlag.class, x -> x.entityEvent(e, e.getEntity()));
	}

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player && e.getCause() != RemoveCause.EXPLOSION)
			fireEvent(e.getEntity().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, (Player) e.getRemover(), e.getEntity()));
		else
			fireEvent(e.getEntity().getLocation(), PhysicsFlag.class, x -> x.entityEvent(e, e.getEntity()));
	}

	@EventHandler
	public void onEntityPickup(EntityPickupItemEvent e) {
		if (e.getEntity() instanceof Player)
			fireEvent(e.getEntity().getLocation(), ItemPickupFlag.class, x -> x.itemPickupEvent(e));
		else
			fireEvent(e.getEntity().getLocation(), PhysicsFlag.class, x -> x.entityEvent(e, e.getEntity()));
	}
	
	@EventHandler
	public void onLecternTake(PlayerTakeLecternBookEvent e) {
		fireEvent(e.getLectern().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getLectern().getBlock()));
	}
	
	@EventHandler
	public void onEntityChangeBlock(EntityChangeBlockEvent e) {
		if (e.getEntity() instanceof Player)
			fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, (Player) e.getEntity(), e.getBlock()));
		else
			fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
		
	}

	@EventHandler
	public void onEntityInteract(EntityInteractEvent e) {
		if (e.getBlock().getType() == Material.FARMLAND)
			if (e.getEntity() instanceof Player)
				fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, (Player) e.getEntity(), e.getBlock()));
			else
				fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onFade(BlockFadeEvent e) {
		if (e.getBlock().getType() == Material.FROSTED_ICE)
			fireEvent(e.getBlock().getLocation(), FrostWalkerFlag.class, x -> x.meltEvent(e));
		else
			fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onBurn(BlockBurnEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onExplode(BlockExplodeEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onForm(BlockFormEvent e) {
		if (e instanceof EntityBlockFormEvent)
			return;
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onEntityForm(EntityBlockFormEvent e) {
		if (e.getEntity() instanceof Player && e.getNewState().getType() == Material.FROSTED_ICE)
			fireEvent(e.getBlock().getLocation(), FrostWalkerFlag.class, x -> x.formEvent(e));
		else
			fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onFromTo(BlockFromToEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onGrow(BlockGrowEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onSpread(BlockSpreadEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onFluidChanges(FluidLevelChangeEvent e) {
		if (BypassFluidsCommand.doBypassFluids(e.getBlock().getWorld()))
			return;
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onLeavesDecay(LeavesDecayEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onMoisture(MoistureChangeEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onSponge(SpongeAbsorbEvent e) {
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}

	@EventHandler
	public void onInteract(PlayerInteractEvent e) {
		if (e.getClickedBlock() == null)
			return;
		if (e.getHand() == EquipmentSlot.OFF_HAND)
			return;
		
		boolean block = false;
		boolean interact = false;
		Material type = e.getClickedBlock().getType();
		if (e.getAction() == Action.PHYSICAL) {
			if (type == Material.FARMLAND || type == Material.TURTLE_EGG) {
				block = true;
			}else interact = true;
		}else if (e.getAction() == Action.RIGHT_CLICK_BLOCK) {
			if (type == Material.DAYLIGHT_DETECTOR || type == Material.REDSTONE_WIRE || type == Material.COMPARATOR || type == Material.REPEATER || type == Material.FLOWER_POT || type == Material.NOTE_BLOCK || type == Material.JUKEBOX || type.name().endsWith("_SIGN") || type.name().startsWith("POTTED_")) {
				block = true;
			}else interact = true;
		}else return;
		
		if (block)
			fireEvent(e.getClickedBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getClickedBlock()));
		if (interact)
			fireEvent(e.getClickedBlock().getLocation(), PlayerBlockInteractFlag.class, x -> x.interactEvent(e));
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntityType() == EntityType.PAINTING || e.getEntityType() == EntityType.ITEM_FRAME || e.getEntityType() == EntityType.ARMOR_STAND)
			fireEvent(e.getEntity().getLocation(), PhysicsFlag.class, x -> x.entityEvent(e, e.getEntity()));
		fireEvent(e.getEntity().getLocation(), DamageFlag.class, x -> x.damageEvent(e));
	}

	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntityType() == EntityType.ARMOR_STAND && e.getDamager() instanceof Player)
			fireEvent(e.getEntity().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, (Player) e.getDamager(), e.getEntity()));
	}

	@EventHandler
	public void onFood(FoodLevelChangeEvent e) {
		fireEvent(e.getEntity().getLocation(), FoodFlag.class, x -> x.foodEvent(e));
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		fireEvent(e.getPlayer().getLocation(), DropFlag.class, x -> x.dropEvent(e));
	}

	@EventHandler
	public void onItemDamage(PlayerItemDamageEvent e) {
		fireEvent(e.getPlayer().getLocation(), ItemDurabilityFlag.class, x -> x.itemDamageEvent(e));
	}

	@EventHandler
	public void onFish(PlayerFishEvent e) {
		fireEvent(e.getPlayer().getLocation(), FishFlag.class, x -> x.fishEvent(e));
	}
	
	@EventHandler
	public void onPistonExtend(BlockPistonExtendEvent e) {
		fireEvent(e.getBlock().getLocation(), RedstoneFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}
	
	@EventHandler
	public void onPistonRetract(BlockPistonRetractEvent e) {
		fireEvent(e.getBlock().getLocation(), RedstoneFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}
	
	@EventHandler
	public void onPreDispense(BlockPreDispenseEvent e) {
		fireEvent(e.getBlock().getLocation(), RedstoneFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}
	
	@EventHandler
	public void onRedstone(BlockRedstoneEvent e) {
		fireEvent(e.getBlock().getLocation(), RedstoneFlag.class, x -> x.redstoneEvent(e));
	}

	@Override
	public boolean disable(OlympaCore plugin) {
		for (World world : Bukkit.getWorlds())
			unregisterWorld(world);
		trackedRegions.clear();
		worldRegions.clear();
		trackAwait.clear();
		inRegions.clear();
		trackedRegions = null;
		worldRegions = null;
		trackAwait = null;
		inRegions = null;
		enable = false;
		return !enable;
	}

	@Override
	public boolean enable(OlympaCore plugin) {
		trackedRegions = new HashMap<>();
		worldRegions = new HashMap<>();
		trackAwait = new HashMap<>();
		inRegions = new HashMap<>();
		plugin.getTask().runTask(() -> Bukkit.getWorlds().forEach(this::registerWorld));
		enable = true;
		return enable;
	}

	@Override
	public boolean setToPlugin(OlympaCore plugin) {
		plugin.setRegionManager(this);
		return true;
	}

	@Override
	public boolean isEnabled() {
		return enable;
	}

}
