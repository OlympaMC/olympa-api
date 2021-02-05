package fr.olympa.api.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.task.TaskManager;

public abstract class OlympaAPIPlugin extends JavaPlugin implements OlympaPluginInterface {

	protected final OlympaTask task;
	protected CustomConfig config;

	public OlympaAPIPlugin() {
		task = new TaskManager(this);
	}

	@Override
	public CustomConfig getConfig() {
		return config;
	}

	@Override
	public String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	@Override
	public OlympaTask getTask() {
		return task;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		config = new CustomConfig(this, "config");
		if (config.hasResource() || config.getFile().exists()) {
			config.load();
			config.saveIfNotExists();
		} else
			config = null;
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getServer().getConsoleSender().sendMessage(ColorUtils.color(String.format(getPrefixConsole() + message, args)));
	}

}
