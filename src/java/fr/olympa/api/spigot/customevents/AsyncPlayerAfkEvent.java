package fr.olympa.api.spigot.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.common.player.OlympaPlayer;

public class AsyncPlayerAfkEvent extends Event {
	
	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final private Player p;
	final private boolean isAfk;

	public AsyncPlayerAfkEvent(Player p, boolean isAfk) {
		super(true);
		this.p = p;
		this.isAfk = isAfk;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public Player getPlayer() {
		return  p;
	}

	public boolean isAfk() {
		return isAfk;
	}
}
