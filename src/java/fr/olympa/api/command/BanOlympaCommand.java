package fr.olympa.api.command;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.permission.OlympaPermission;

public abstract class BanOlympaCommand extends OlympaCommand {

	public BanOlympaCommand(Plugin plugin, String command, OlympaPermission permission, String... alias) {
		super(plugin, command, permission, alias);
	}

	public BanOlympaCommand(Plugin plugin, String command, String... alias) {
		super(plugin, command, alias);
	}

	public BanOlympaCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... alias) {
		super(plugin, command, description, permission, alias);
	}
}
