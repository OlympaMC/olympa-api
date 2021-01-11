package fr.olympa.api.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.task.TaskManager;

public abstract class OlympaAPIPlugin extends JavaPlugin implements OlympaPluginInterface {

	protected final OlympaTask task;
	protected CustomConfig config;
	protected INametagApi nameTagApi;
	private OlympaServer olympaServer = OlympaServer.ALL;

	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	public void setOlympaServer(OlympaServer olympaServer) {
		this.olympaServer = olympaServer;
	}

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
		while (Bukkit.getOnlinePlayers().size() > 0) {
			for (Player p : Bukkit.getOnlinePlayers())
				p.kickPlayer("Server closed");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getServer().getConsoleSender().sendMessage(ColorUtils.color(String.format(getPrefixConsole() + message, args)));
	}

}
