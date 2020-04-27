package fr.olympa.api.customevents;

import org.bukkit.Location;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerMoveBlockXZEvent extends PlayerEvent {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private Location from;
	private Location to;

	public PlayerMoveBlockXZEvent(PlayerMoveEvent event) {
		super(event.getPlayer());
		from = event.getFrom();
		to = event.getTo();
	}

	public Location getFrom() {
		return from;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Location getTo() {
		return to;
	}

}
