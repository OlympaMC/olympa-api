package fr.olympa.api.customevents;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;

public class AsyncOlympaPlayerChangeGroupEvent extends Event {

	public enum ChangeType {

		ADD(1),
		REMOVE(2),
		SET(3);

		public static ChangeType get(int i) {
			return Arrays.stream(ChangeType.values()).filter(ct -> ct.getState() == i).findFirst().orElse(null);
		}

		int state;

		private ChangeType(int state) {
			this.state = state;
		}

		public int getState() {
			return state;
		}

		public void setState(int state) {
			this.state = state;
		}

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

		isAsynchronous();
	}

	public ChangeType getChangeType() {
		return olympaGroupChangeType;
	}

	public OlympaGroup getGroupChange() {
		return groupChange;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	public Player getPlayer() {
		return player;
	}
}
