package fr.olympa.api.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.task.TaskManager;
import fr.olympa.api.utils.ColorUtils;

public abstract class OlympaAPIPlugin extends JavaPlugin implements OlympaPluginInterface {

	protected final OlympaTask task;
	protected CustomConfig config;
	protected INametagApi nameTagApi;

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

	public INametagApi getNameTagApi() {
		return nameTagApi;
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
		for (Player p : Bukkit.getOnlinePlayers())
			p.kickPlayer("Server closed");
	}

	@Override
	public void sendMessage(String message) {
		getServer().getConsoleSender().sendMessage(ColorUtils.color(getPrefixConsole() + message));
	}

}
