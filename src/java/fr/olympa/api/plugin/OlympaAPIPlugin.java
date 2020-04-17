package fr.olympa.api.plugin;

import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.task.TaskManager;
import fr.olympa.api.utils.ColorUtils;

public abstract class OlympaAPIPlugin extends JavaPlugin implements OlympaPluginInterface {

	protected final OlympaTask task;
	protected CustomConfig config;

	public OlympaAPIPlugin() {
		this.task = new TaskManager(this);
	}

	@Override
	public void onEnable() {
		super.onEnable();
		this.config = new CustomConfig(this, "config");
		if (this.config.hasResource() || this.config.getFile().exists()) {
			this.config.load();
			this.config.saveIfNotExists();
		}else this.config = null;
	}

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
		this.getServer().getConsoleSender().sendMessage(ColorUtils.color(this.getPrefixConsole() + message));
	}

}
