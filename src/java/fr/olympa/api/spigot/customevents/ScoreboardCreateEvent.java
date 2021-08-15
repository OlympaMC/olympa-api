package fr.olympa.api.spigot.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.scoreboard.sign.Scoreboard;

public class ScoreboardCreateEvent<T extends OlympaPlayer> extends Event {

	private final T olympaPlayer;
	private final Scoreboard<T> scoreboard;
	private final Reason reason;

	public ScoreboardCreateEvent(T olympaPlayer, Scoreboard<T> scoreboard, boolean async, Reason reason) {
		super(async);
		this.olympaPlayer = olympaPlayer;
		this.scoreboard = scoreboard;
		this.reason = reason;
	}

	public Player getPlayer() {
		return (Player) olympaPlayer.getPlayer();
	}

	public T getOlympaPlayer() {
		return olympaPlayer;
	}

	public Scoreboard<T> getScoreboard() {
		return scoreboard;
	}
	
	public Reason getReason() {
		return reason;
	}

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}
	
	public enum Reason {
		JOIN, RESET;
	}

}
