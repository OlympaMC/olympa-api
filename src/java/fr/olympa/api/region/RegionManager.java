package fr.olympa.api.region;

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

import fr.olympa.api.utils.SpigotUtils;

public class RegionManager implements Listener {

	private Set<Region> trackedRegions = new HashSet<>();

	private Map<Player, Set<Region>> inRegions = new HashMap<>();

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (trackedRegions.isEmpty()) return;
		if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) return;

		Player player = e.getPlayer();

		Set<Region> applicable = trackedRegions.stream().filter(x -> x.isIn(e.getTo())).collect(Collectors.toSet());
		Set<Region> lastRegions = inRegions.get(player);
		if (lastRegions == null) lastRegions = Collections.EMPTY_SET;

		Set<Region> entered = Sets.difference(applicable, lastRegions);
		Set<Region> exited = Sets.difference(lastRegions, applicable);

		for (Region enter : entered) {
			if (enter.getEnterPredicate().test(player)) e.setCancelled(true);
		}
		for (Region exit : exited) {
			if (exit.getExitPredicate().test(player)) e.setCancelled(true);
		}
		
		if (!entered.isEmpty() || !exited.isEmpty()) inRegions.put(player, applicable);
	}

	public void registerRegion(Region region) {
		trackedRegions.add(region);
	}

}
