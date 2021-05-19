package fr.olympa.api.region.tracking;

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
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFertilizeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingBreakEvent;
import org.bukkit.event.hanging.HangingBreakEvent.RemoveCause;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.google.common.collect.Sets;

import fr.olympa.api.customevents.AsyncPlayerMoveRegionsEvent;
import fr.olympa.api.customevents.WorldTrackingEvent;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.shapes.WorldRegion;
import fr.olympa.api.region.tracking.flags.DamageFlag;
import fr.olympa.api.region.tracking.flags.DropFlag;
import fr.olympa.api.region.tracking.flags.FishFlag;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.api.region.tracking.flags.FoodFlag;
import fr.olympa.api.region.tracking.flags.FrostWalkerFlag;
import fr.olympa.api.region.tracking.flags.ItemDurabilityFlag;
import fr.olympa.api.region.tracking.flags.PhysicsFlag;
import fr.olympa.api.region.tracking.flags.PlayerBlockInteractFlag;
import fr.olympa.api.region.tracking.flags.PlayerBlocksFlag;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class RegionManager implements Listener {

	/**
	 * Compare les priorités des régions de la plus faible à la plus forte
	 */
	public static final Comparator<TrackedRegion> REGION_COMPARATOR = (o1, o2) -> Integer.compare(o1.getPriority().getSlot(), o2.getPriority().getSlot());
	
	/**
	 * Compare les priorités des régions de la plus forte à la plus faible
	 */
	public static final Comparator<TrackedRegion> REGION_COMPARATOR_INVERT = (o1, o2) -> Integer.compare(o2.getPriority().getSlot(), o1.getPriority().getSlot());
	
	private final Map<String, TrackedRegion> trackedRegions = new HashMap<>();
	private final Map<World, TrackedRegion> worldRegions = new HashMap<>();
	
	private final Map<String, Consumer<WorldTrackingEvent>> trackAwait = new HashMap<>();

	private final Map<Player, Set<TrackedRegion>> inRegions = new HashMap<>();

	public RegionManager() {
		Bukkit.getScheduler().runTask(OlympaCore.getInstance(), () -> {
			for (World world : Bukkit.getWorlds()) {
				registerWorld(world);
			}
		});
		
		new BypassCommand().register();
	}

	public TrackedRegion registerRegion(Region region, String id, EventPriority priority, Flag... flags) {
		Validate.notNull(region, id + " is a null region");
		Validate.isTrue(!id.endsWith("_global"), "The region ID cannot end with _global");
		Validate.isTrue(!trackedRegions.containsKey(id), "A region with ID " + id + " already exists");
		TrackedRegion tracked = new TrackedRegion(region, id, priority, flags);
		trackedRegions.put(id, tracked);
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (region.isIn(p)) {
				Set<TrackedRegion> regionsSet = null;
				for (Flag flag : flags) {
					if (regionsSet == null) regionsSet = getApplicableRegions(p.getLocation());
					if (flag.enters(p, regionsSet) == ActionResult.DENY) OlympaCore.getInstance().getLogger().warning("Entry cancelled when new region - ignored.");
				}
			}
		}
		return tracked;
	}

	public void unregisterRegion(String id) {
		trackedRegions.remove(id);
	}

	private void registerWorld(World world) {
		if (worldRegions.containsKey(world)) return;
		TrackedRegion region = new TrackedRegion(new WorldRegion(world), world.getName() + "_global", EventPriority.LOWEST);
		worldRegions.put(world, region);
		trackedRegions.put(region.getID(), region);
		OlympaCore.getInstance().sendMessage("Région globale enregistrée pour le monde §e" + world.getName());
		WorldTrackingEvent event = new WorldTrackingEvent(world, region);
		Bukkit.getPluginManager().callEvent(event);
		Consumer<WorldTrackingEvent> consumer = trackAwait.remove(world.getName());
		if (consumer != null) consumer.accept(event);
	}

	private void unregisterWorld(World world) {
		TrackedRegion region = worldRegions.remove(world);
		if (region == null) return;
		unregisterRegion(region.getID());
		OlympaCore.getInstance().sendMessage("Région globale supprimée pour le monde §e" + world.getName());
	}
	
	public void awaitWorldTracking(String worldName, Consumer<WorldTrackingEvent> consumer) {
		trackAwait.put(worldName, consumer);
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
		return trackedRegions.values().stream().filter(x -> x.getRegion().isIn(location)).collect(Collectors.toUnmodifiableSet());
	}

	public <T extends Flag> void fireEvent(Location location, Class<T> flagClass, Consumer<T> consumer) {
		trackedRegions.values().stream().filter(x -> x.getRegion().isIn(location)).sorted(REGION_COMPARATOR).forEach(x -> {
			T flag = x.getFlag(flagClass);
			if (flag != null) consumer.accept(flag);
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
			if (wasIn == isIn) continue;
			if (wasIn) {
				Set<TrackedRegion> regionsSet = getCachedPlayerRegions(p);
				regionsSet.remove(region);
				for (Flag flag : region.getFlags()) {
					if (flag.leaves(p, regionsSet) == ActionResult.DENY) OlympaCore.getInstance().getLogger().warning("Leave cancelled when region editing - ignored.");
				}
			}else { // isIn
				Set<TrackedRegion> regionsSet = getCachedPlayerRegions(p);
				regionsSet.add(region);
				for (Flag flag : region.getFlags()) {
					if (flag.enters(p, regionsSet) == ActionResult.DENY) OlympaCore.getInstance().getLogger().warning("Entry cancelled when region editing - ignored.");
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onWorldLoad(WorldInitEvent e) {
		registerWorld(e.getWorld());
	}

	@EventHandler
	public void onWorldUnload(WorldUnloadEvent e) {
		unregisterWorld(e.getWorld());
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		Set<TrackedRegion> regions = getApplicableRegions(e.getPlayer().getLocation());
		inRegions.put(e.getPlayer(), regions);
		if (!e.getPlayer().hasPlayedBefore()) {
			for (TrackedRegion enter : regions) {
				try {
					for (Flag flag : enter.getFlags()) {
						if (flag.enters(e.getPlayer(), regions) == ActionResult.DENY) OlympaCore.getInstance().getLogger().warning("Entry cancelled when first join - ignored.");
					}
				}catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (trackedRegions.isEmpty()) return;
		if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) return;

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
			
			ActionResult result = ActionResult.ALLOW;
			
			for (TrackedRegion enter : entered) {
				try {
					for (Flag flag : enter.getFlags()) {
						result = result.or(flag.enters(player, applicable));
					}
				}catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			for (TrackedRegion exit : exited) {
				try {
					for (Flag flag : exit.getFlags()) {
						result = result.or(flag.leaves(player, applicable));
					}
				}catch (Exception ex) {
					ex.printStackTrace();
				}
			}
			
			if (result == ActionResult.DENY) break;
			if (result == ActionResult.TELEPORT_ELSEWHERE) return;
			
			if (!entered.isEmpty() || !exited.isEmpty()) madeChange = true;
			lastRegions = applicable;
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
		if (e.getRightClicked() instanceof ItemFrame || e.getRightClicked() instanceof ArmorStand) fireEvent(e.getRightClicked().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, e.getPlayer(), e.getRightClicked()));
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
		if (e instanceof HangingBreakByEntityEvent) return;
		fireEvent(e.getEntity().getLocation(), PhysicsFlag.class, x -> x.entityEvent(e, e.getEntity()));
	}

	@EventHandler
	public void onHangingBreakByEntity(HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player && e.getCause() != RemoveCause.EXPLOSION) {
			fireEvent(e.getEntity().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, (Player) e.getRemover(), e.getEntity()));
		}else fireEvent(e.getEntity().getLocation(), PhysicsFlag.class, x -> x.entityEvent(e, e.getEntity()));
	}

	@EventHandler
	public void onEntityInteract(EntityInteractEvent e) {
		if (e.getBlock().getType() == Material.FARMLAND) {
			if (e.getEntity() instanceof Player) {
				fireEvent(e.getBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, (Player) e.getEntity(), e.getBlock()));
			}else fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
		}
	}

	@EventHandler
	public void onFade(BlockFadeEvent e) {
		if (e.getBlock().getType() == Material.FROSTED_ICE) {
			fireEvent(e.getBlock().getLocation(), FrostWalkerFlag.class, x -> x.meltEvent(e));
		}else {			
			fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
		}
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
		if (e instanceof EntityBlockFormEvent) return;
		fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
	}
	
	@EventHandler
	public void onEntityForm(EntityBlockFormEvent e) {
		if (e.getEntity() instanceof Player && e.getNewState().getType() == Material.FROSTED_ICE) {
			fireEvent(e.getBlock().getLocation(), FrostWalkerFlag.class, x -> x.formEvent(e));
		}else {
			fireEvent(e.getBlock().getLocation(), PhysicsFlag.class, x -> x.blockEvent(e, e.getBlock()));
		}
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
		if (e.getClickedBlock() == null) return;
		if (e.getHand() == EquipmentSlot.OFF_HAND) return;
		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) return;
		Material type = e.getClickedBlock().getType();
		if (type == Material.DAYLIGHT_DETECTOR || type == Material.REDSTONE_WIRE || type == Material.COMPARATOR || type == Material.REPEATER || type == Material.FLOWER_POT || type == Material.NOTE_BLOCK || type == Material.JUKEBOX || type.name().endsWith("_SIGN") || type.name().startsWith("POTTED_")) {
			fireEvent(e.getClickedBlock().getLocation(), PlayerBlocksFlag.class, x -> x.blockEvent(e, e.getPlayer(), e.getClickedBlock()));
		}else fireEvent(e.getClickedBlock().getLocation(), PlayerBlockInteractFlag.class, x -> x.interactEvent(e));
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		if (e.getEntityType() == EntityType.PAINTING || e.getEntityType() == EntityType.ITEM_FRAME || e.getEntityType() == EntityType.ARMOR_STAND) {
			fireEvent(e.getEntity().getLocation(), PhysicsFlag.class, x -> x.entityEvent(e, e.getEntity()));
		}
		fireEvent(e.getEntity().getLocation(), DamageFlag.class, x -> x.damageEvent(e));
	}
	
	@EventHandler
	public void onDamage(EntityDamageByEntityEvent e) {
		if (e.getEntityType() == EntityType.ARMOR_STAND && e.getDamager() instanceof Player) fireEvent(e.getEntity().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, (Player) e.getDamager(), e.getEntity()));
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
	
}
