package fr.olympa.api.bungee.config;

import net.md_5.bungee.api.plugin.Event;
import net.md_5.bungee.config.Configuration;

public class BungeeConfigReloadEvent extends Event {

	final private Configuration config;
	final private String fileName;

	public BungeeConfigReloadEvent(String fileName, Configuration config) {
		this.fileName = fileName;
		this.config = config;
	}

	public String getFileName() {
		return fileName;
	}

	public Configuration getConfig() {
		return config;
	}
}
