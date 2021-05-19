package fr.olympa.api.customevents;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.google.common.collect.Sets;

import fr.olympa.api.region.tracking.TrackedRegion;

public class AsyncPlayerMoveRegionsEvent extends Event { // no PlayerEvent bc cannot be async
	
	private final Player who;
	private final Set<TrackedRegion> regions;
	private final Set<TrackedRegion> lastRegions;
	
	// Pas initialisés la première fois pour pas faire de calculs inutiles au cas où aucun plugin n'a besoin de ces différences
	private Set<TrackedRegion> entered;
	private Set<TrackedRegion> exited;
	private Set<TrackedRegion> difference;
	
	public AsyncPlayerMoveRegionsEvent(Player who, Set<TrackedRegion> regions, Set<TrackedRegion> lastRegions) {
		super(true);
		this.who = who;
		this.regions = regions;
		this.lastRegions = lastRegions;
	}
	
	public Player getPlayer() {
		return who;
	}
	
	public Set<TrackedRegion> getRegions() {
		return regions;
	}
	
	public Set<TrackedRegion> getLastRegions() {
		return lastRegions;
	}
	
	public Set<TrackedRegion> getEntered() {
		return entered == null ? entered = Sets.difference(regions, lastRegions) : entered;
	}
	
	public Set<TrackedRegion> getExited() {
		return exited == null ? exited = Sets.difference(lastRegions, regions) : exited;
	}
	
	public Set<TrackedRegion> getDifference() {
		return difference == null ? difference = Sets.symmetricDifference(lastRegions, regions) : difference;
	}
	
	public static final HandlerList handlers = new HandlerList();
	
	public static HandlerList getHandlerList() {
		return handlers;
	}
	
	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
}
