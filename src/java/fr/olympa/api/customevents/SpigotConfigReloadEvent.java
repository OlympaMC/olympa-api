package fr.olympa.api.customevents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.config.CustomConfig;

public class SpigotConfigReloadEvent extends Event {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final private CustomConfig customConfig;

	public SpigotConfigReloadEvent(CustomConfig customConfig) {
		super(true);
		this.customConfig = customConfig;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public CustomConfig getConfig() {
		return customConfig;
	}

}