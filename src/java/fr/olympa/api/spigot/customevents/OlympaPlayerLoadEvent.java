package fr.olympa.api.spigot.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.common.player.OlympaPlayer;

public class OlympaPlayerLoadEvent extends Event {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final private OlympaPlayer olympaPlayer;
	final private Player player;

	public OlympaPlayerLoadEvent(Player who, OlympaPlayer olympaPlayer, boolean async) {
		super(async);
		player = who;
		this.olympaPlayer = olympaPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return (T) olympaPlayer;
	}

	public Player getPlayer() {
		return player;
	}
}