package fr.olympa.api.region.tracking;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
import org.bukkit.event.block.FluidLevelChangeEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.block.MoistureChangeEvent;
import org.bukkit.event.block.SpongeAbsorbEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.hanging.HangingPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.inventory.EquipmentSlot;

import com.google.common.collect.Sets;

import fr.olympa.api.customevents.WorldTrackingEvent;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.shapes.WorldRegion;
import fr.olympa.api.region.tracking.flags.DamageFlag;
import fr.olympa.api.region.tracking.flags.DropFlag;
import fr.olympa.api.region.tracking.flags.Flag;
import fr.olympa.api.region.tracking.flags.FoodFlag;
import fr.olympa.api.region.tracking.flags.PhysicsFlag;
import fr.olympa.api.region.tracking.flags.PlayerBlocksFlag;
import fr.olympa.api.region.tracking.flags.PlayerInteractFlag;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class RegionManager implements Listener {

	private final Map<String, TrackedRegion> trackedRegions = new HashMap<>();
	private final Map<World, TrackedRegion> worldRegions = new HashMap<>();

	private final Map<Player, Set<TrackedRegion>> inRegions = new HashMap<>();

	public RegionManager() {
		Bukkit.getScheduler().runTask(OlympaCore.getInstance(), () -> {
			for (World world : Bukkit.getWorlds()) {
				registerWorld(world);
			}
		});
	}

	private void registerWorld(World world) {
		if (worldRegions.containsKey(world)) return;
		TrackedRegion region = new TrackedRegion(new WorldRegion(world), world.getName() + "_global", EventPriority.LOWEST);
		worldRegions.put(world, region);
		trackedRegions.put(region.getID(), region);
		OlympaCore.getInstance().getLogger().info("Registered global region for world " + world.getName());
		Bukkit.getPluginManager().callEvent(new WorldTrackingEvent(world, region));
	}

	public TrackedRegion registerRegion(Region region, String id, EventPriority priority, Flag... flags) {
		Validate.notNull(region, id + " is a null region");
		Validate.isTrue(!id.endsWith("_global"), "The region ID cannot end with _global");
		Validate.isTrue(!trackedRegions.containsKey(id), "A region with ID" + id + " already exists");
		TrackedRegion tracked = new TrackedRegion(region, id, priority, flags);
		trackedRegions.put(id, tracked);
		return tracked;
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
		trackedRegions.values().stream().filter(x -> x.getRegion().isIn(location)).sorted(RegionComparator.COMPARATOR).forEach(x -> {
			T flag = x.getFlag(flagClass);
			if (flag != null) consumer.accept(flag);
		});
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onWorldLoad(WorldInitEvent e) {
		registerWorld(e.getWorld());
	}

	@EventHandler (priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		Set<TrackedRegion> regions = getApplicableRegions(e.getPlayer().getLocation());
		inRegions.put(e.getPlayer(), regions);
		if (!e.getPlayer().hasPlayedBefore()) {
			for (TrackedRegion enter : regions) {
				try {
					for (Flag flag : enter.getFlags()) {
						if (flag.enters(e.getPlayer(), regions)) OlympaCore.getInstance().getLogger().warning("Entry cancelled when first join - ignored.");
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

		Set<TrackedRegion> applicable = getApplicableRegions(e.getTo());
		Set<TrackedRegion> lastRegions = inRegions.get(player);

		Set<TrackedRegion> entered = Sets.difference(applicable, lastRegions);
		Set<TrackedRegion> exited = Sets.difference(lastRegions, applicable);

		for (TrackedRegion enter : entered) {
			try {
				for (Flag flag : enter.getFlags()) {
					if (flag.enters(player, applicable)) e.setCancelled(true);
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (TrackedRegion exit : exited) {
			try {
				for (Flag flag : exit.getFlags()) {
					if (flag.leaves(player, applicable)) e.setCancelled(true);
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (!entered.isEmpty() || !exited.isEmpty()) inRegions.put(player, applicable);
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
		if (e.getRightClicked() instanceof ItemFrame) fireEvent(e.getRightClicked().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, e.getPlayer(), e.getRightClicked()));
	}

	@EventHandler
	public void onHangingPlace(HangingPlaceEvent e) {
		fireEvent(e.getEntity().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, e.getPlayer(), e.getEntity()));
	}

	@EventHandler
	public void onHangingBreak(HangingBreakByEntityEvent e) {
		if (e.getRemover() instanceof Player) fireEvent(e.getEntity().getLocation(), PlayerBlocksFlag.class, x -> x.entityEvent(e, (Player) e.getRemover(), e.getEntity()));
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
		fireEvent(e.getClickedBlock().getLocation(), PlayerInteractFlag.class, x -> x.interactEvent(e));
	}

	@EventHandler
	public void onDamage(EntityDamageEvent e) {
		fireEvent(e.getEntity().getLocation(), DamageFlag.class, x -> x.damageEvent(e));
	}
	
	@EventHandler
	public void onFood(FoodLevelChangeEvent e) {
		fireEvent(e.getEntity().getLocation(), FoodFlag.class, x -> x.foodEvent(e));
	}

	@EventHandler
	public void onDrop(PlayerDropItemEvent e) {
		fireEvent(e.getPlayer().getLocation(), DropFlag.class, x -> x.dropEvent(e));
	}

}
