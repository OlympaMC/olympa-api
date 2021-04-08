package fr.olympa.api.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.player.OlympaPlayer;

public class PlayerAfkEvent extends Event {
	
	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final private Player p;
	final private boolean isAfk;

	public PlayerAfkEvent(Player p, boolean isAfk) {
		super(false);
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
