package fr.olympa.api.report;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class OlympaReportAddEvent extends PlayerEvent {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final OlympaReport report;
	private final OfflinePlayer target;

	public OlympaReportAddEvent(Player who, OfflinePlayer target, OlympaReport report) {
		super(who);
		this.target = target;
		this.report = report;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public OlympaReport getReport() {
		return report;
	}

	public OfflinePlayer getTarget() {
		return target;
	}

}
