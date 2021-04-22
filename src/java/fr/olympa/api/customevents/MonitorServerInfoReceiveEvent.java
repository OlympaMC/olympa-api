package fr.olympa.api.customevents;

import java.util.List;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.server.MonitorInfo;

public class MonitorServerInfoReceiveEvent extends Event {

	private List<MonitorInfo> servers;

	public MonitorServerInfoReceiveEvent(List<MonitorInfo> servers) {
		super(true);
		this.servers = servers;
	}

	public List<MonitorInfo> getServers() {
		return servers;
	}

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

}