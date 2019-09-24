package fr.tristiisch.olympa.api.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;

public class AsyncOlympaPlayerLoadEvent extends PlayerEvent {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final private OlympaPlayer olympaPlayer;

	public AsyncOlympaPlayerLoadEvent(Player who, OlympaPlayer olympaPlayer) {
		super(who);
		this.olympaPlayer = olympaPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public OlympaPlayer getOlympaPlayer() {
		return this.olympaPlayer;
	}
}