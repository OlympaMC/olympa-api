package fr.olympa.api.customevents;

import java.util.Arrays;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.player.OlympaPlayer;

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

	public class ChangeSource {

		String source;
		String player;

		public ChangeSource() {
			source = "console";
		}

		public ChangeSource(String player) {
			this.player = player;
		}

		public String getSource() {
			return source != null ? source : player;
		}

		public boolean isPlayer() {
			return player != null;
		}

		public boolean isConsole() {
			return source != null && source.equals("console");
		}

		public boolean isSite() {
			return source != null && source.equals("site");
		}

		public String getPlayer() {
			return player;
		}

	}

	public static HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	ChangeType olympaGroupChangeType;

	private OlympaPlayer olympaPlayer;
	private Player player;
	private OlympaGroup[] groupsChanges;
	private long timeStamp;
	private Consumer<? super Boolean> done;

	public long getTimeStamp() {
		return timeStamp;
	}

	public Consumer<? super Boolean> getDone() {
		return done;
	}

	public AsyncOlympaPlayerChangeGroupEvent(Player player, ChangeType olympaGroupChangeType, OlympaPlayer olympaPlayer, OlympaGroup... groupsChanges) {
		super(true);
		this.player = player;
		this.olympaGroupChangeType = olympaGroupChangeType;
		this.olympaPlayer = olympaPlayer;
		this.groupsChanges = groupsChanges;
	}

	public AsyncOlympaPlayerChangeGroupEvent(Player player, ChangeType olympaGroupChangeType, OlympaPlayer olympaPlayer, Consumer<? super Boolean> done, long timeStamp, OlympaGroup... groupsChanges) {
		super(true);
		this.player = player;
		this.olympaGroupChangeType = olympaGroupChangeType;
		this.olympaPlayer = olympaPlayer;
		this.timeStamp = timeStamp;
		this.done = done;
		this.groupsChanges = groupsChanges;
	}

	public ChangeType getChangeType() {
		return olympaGroupChangeType;
	}

	public OlympaGroup[] getGroupsChanges() {
		return groupsChanges;
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
