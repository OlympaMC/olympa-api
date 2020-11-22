package fr.olympa.api.customevents;

import java.util.Set;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.region.tracking.TrackedRegion;

public class AsyncPlayerMoveRegionsEvent extends Event {
	
	private Player who;
	private Set<TrackedRegion> regions;
	
	public AsyncPlayerMoveRegionsEvent(Player who, Set<TrackedRegion> regions) {
		this.who = who;
		this.regions = regions;
	}
	
	public Player getPlayer() {
		return who;
	}
	
	public Set<TrackedRegion> getRegions() {
		return regions;
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
