package fr.olympa.api.region.tracking;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.collect.Sets;

import fr.olympa.api.region.Region;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class RegionManager implements Listener {

	private Set<TrackedRegion> trackedRegions = new HashSet<>();

	private Map<Player, Set<TrackedRegion>> inRegions = new HashMap<>();

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (trackedRegions.isEmpty()) return;
		if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) return;

		Player player = e.getPlayer();

		Set<TrackedRegion> applicable = trackedRegions.stream().filter(x -> x.getRegion().isIn(e.getTo())).collect(Collectors.toSet());
		Set<TrackedRegion> lastRegions = inRegions.get(player);
		if (lastRegions == null) lastRegions = Collections.EMPTY_SET;

		Set<TrackedRegion> entered = Sets.difference(applicable, lastRegions);
		Set<TrackedRegion> exited = Sets.difference(lastRegions, applicable);

		for (TrackedRegion enter : entered) {
			try {
				for (Flag flag : enter.getFlags()) {
					if (flag.enters(player)) e.setCancelled(true);
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		for (TrackedRegion exit : exited) {
			try {
				for (Flag flag : exit.getFlags()) {
					if (flag.leaves(player)) e.setCancelled(true);
				}
			}catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		if (!entered.isEmpty() || !exited.isEmpty()) inRegions.put(player, applicable);
	}

	public TrackedRegion registerRegion(Region region, String id, Flag... flags) {
		TrackedRegion tracked = new TrackedRegion(region, id, flags);
		trackedRegions.add(tracked);
		return tracked;
	}

	public boolean isIn(Player p, String id) {
		Set<TrackedRegion> regions = inRegions.get(p);
		return regions.stream().anyMatch(x -> x.getID().equals(id));
	}

	public Set<TrackedRegion> getTrackedRegions() {
		return trackedRegions;
	}

	public Set<TrackedRegion> getCachedPlayerRegions(Player p) {
		return inRegions.get(p);
	}

}
