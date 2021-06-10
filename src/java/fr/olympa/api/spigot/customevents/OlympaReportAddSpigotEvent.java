package fr.olympa.api.spigot.customevents;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import fr.olympa.api.common.report.OlympaReport;

public class OlympaReportAddSpigotEvent extends PlayerEvent {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final OlympaReport report;
	private final OfflinePlayer target;

	public OlympaReportAddSpigotEvent(Player who, OfflinePlayer target, OlympaReport report) {
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
