package fr.olympa.api.region;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import com.google.common.base.Predicates;
import com.google.common.collect.Sets;

import fr.olympa.api.utils.SpigotUtils;

public class RegionManager implements Listener {

	private Set<TrackedRegion> trackedRegions = new HashSet<>();

	private Map<Player, Set<TrackedRegion>> inRegions = new HashMap<>();

	@EventHandler
	public void onMove(PlayerMoveEvent e) {
		if (trackedRegions.isEmpty()) return;
		if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo())) return;

		Player player = e.getPlayer();

		Set<TrackedRegion> applicable = trackedRegions.stream().filter(x -> x.region.isIn(e.getTo())).collect(Collectors.toSet());
		Set<TrackedRegion> lastRegions = inRegions.get(player);
		if (lastRegions == null) lastRegions = Collections.EMPTY_SET;

		Set<TrackedRegion> entered = Sets.difference(applicable, lastRegions);
		Set<TrackedRegion> exited = Sets.difference(lastRegions, applicable);

		for (TrackedRegion enter : entered) {
			if (enter.getEnterPredicate().test(player)) e.setCancelled(true);
		}
		for (TrackedRegion exit : exited) {
			if (exit.getExitPredicate().test(player)) e.setCancelled(true);
		}
		
		if (!entered.isEmpty() || !exited.isEmpty()) inRegions.put(player, applicable);
	}

	public void registerRegion(Region region, String id, Predicate<Player> enterPredicate, Predicate<Player> exitPredicate) {
		trackedRegions.add(new TrackedRegion(region, id, enterPredicate, exitPredicate));
	}

	public Set<TrackedRegion> getTrackedRegions() {
		return trackedRegions;
	}

	public Set<TrackedRegion> getCachedPlayerRegions(Player p) {
		return inRegions.get(p);
	}

	public class TrackedRegion {
		private final Region region;
		private final String id;
		private Predicate<Player> enterPredicate;
		private Predicate<Player> exitPredicate;

		public TrackedRegion(Region region, String id, Predicate<Player> enterPredicate, Predicate<Player> exitPredicate) {
			this.region = region;
			this.id = id;
			this.enterPredicate = enterPredicate == null ? Predicates.alwaysFalse() : enterPredicate;
			this.exitPredicate = exitPredicate == null ? Predicates.alwaysFalse() : exitPredicate;
		}

		public Region getRegion() {
			return region;
		}

		public String getID() {
			return id;
		}

		public Predicate<Player> getEnterPredicate() {
			return enterPredicate;
		}

		public void setEnterPredicate(Predicate<Player> enterPredicate) {
			this.enterPredicate = enterPredicate;
		}

		public Predicate<Player> getExitPredicate() {
			return exitPredicate;
		}

		public void setExitPredicate(Predicate<Player> exitPredicate) {
			this.exitPredicate = exitPredicate;
		}

	}

}
