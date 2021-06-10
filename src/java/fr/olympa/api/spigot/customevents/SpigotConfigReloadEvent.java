package fr.olympa.api.spigot.customevents;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.spigot.config.CustomConfig;

/**
 * {@link Deprecated} Use {@link fr.olympa.api.spigot.config.CustomConfig#addTask(String, java.util.function.Consumer)} instand
 */
@Deprecated(since = "28/05/2020")
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