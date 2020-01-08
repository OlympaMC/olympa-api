package fr.olympa.api.groups;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.objects.OlympaPlayer;

public class AsyncOlympaPlayerChangeGroupEvent extends Event {

	public enum ChangeType {
		ADD,
		REMOVE,
		SET;
	}

	public static HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	ChangeType olympaGroupChangeType;

	private OlympaPlayer olympaPlayer;
	private Player player;
	private OlympaGroup groupChange;

	public AsyncOlympaPlayerChangeGroupEvent(Player player, ChangeType olympaGroupChangeType, OlympaPlayer olympaPlayer, OlympaGroup groupChange) {
		super(true);
		this.player = player;
		this.olympaGroupChangeType = olympaGroupChangeType;
		this.olympaPlayer = olympaPlayer;
		this.groupChange = groupChange;

		this.isAsynchronous();
	}

	public ChangeType getChangeType() {
		return this.olympaGroupChangeType;
	}

	public OlympaGroup getGroupChange() {
		return this.groupChange;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public OlympaPlayer getOlympaPlayer() {
		return this.olympaPlayer;
	}

	public Player getPlayer() {
		return player;
	}
}
