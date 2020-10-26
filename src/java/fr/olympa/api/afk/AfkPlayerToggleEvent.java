package fr.olympa.api.afk;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AfkPlayerToggleEvent extends Event {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	private final AfkPlayer afkPlayer;
	private final Player player;

	public AfkPlayerToggleEvent(Player player, AfkPlayer afkPlayer) {
		this.player = player;
		this.afkPlayer = afkPlayer;
	}

	public Player getPlayer() {
		return player;
	}

	public AfkPlayer getAfkPlayer() {
		return afkPlayer;
	}

}
