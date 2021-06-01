package fr.olympa.api.spigot.customevents;

import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.bukkit.event.world.WorldEvent;

import fr.olympa.api.spigot.region.tracking.TrackedRegion;

public class WorldTrackingEvent extends WorldEvent {

	public static final HandlerList handlers = new HandlerList();

	private final TrackedRegion region;

	public static HandlerList getHandlerList() {
		return handlers;
	}

	public WorldTrackingEvent(World world, TrackedRegion region) {
		super(world);
		this.region = region;
	}

	public TrackedRegion getRegion() {
		return region;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}
