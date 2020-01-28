package fr.olympa.api.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.SpigotUtils;

public abstract class OlympaAPIPlugin extends JavaPlugin implements OlympaPluginInterface {

	protected OlympaTask task;
	protected CustomConfig config;

	@Override
	public CustomConfig getConfig() {
		return this.config;
	}

	@Override
	public String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	@Override
	public OlympaTask getTask() {
		return this.task;
	}

	@Override
	public void sendMessage(String message) {
		this.getServer().getConsoleSender().sendMessage(SpigotUtils.color(this.getPrefixConsole() + message));
	}

}
