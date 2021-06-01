package fr.olympa.api.spigot.region.tracking;

import java.util.Set;

import org.bukkit.entity.Player;

public abstract class RegionEvent {
	
	private final Player player;
	private final Set<TrackedRegion> to;
	private final RegionEventReason reason;
	
	protected RegionEvent(Player player, Set<TrackedRegion> to, RegionEventReason reason) {
		this.player = player;
		this.to = to;
		this.reason = reason;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public Set<TrackedRegion> getRegionsTo() {
		return to;
	}
	
	public RegionEventReason getReason() {
		return reason;
	}
	
	public static class EntryEvent extends RegionEvent {
		
		public EntryEvent(Player player, Set<TrackedRegion> to, RegionEventReason reason) {
			super(player, to, reason);
		}
		
	}
	
	public static class ExitEvent extends RegionEvent {
		
		protected ExitEvent(Player player, Set<TrackedRegion> to, RegionEventReason reason) {
			super(player, to, reason);
		}
		
	}
	
	public enum RegionEventReason {
		JOIN, MOVE, REGION_UPDATE, REGION_CREATION;
	}
	
}
