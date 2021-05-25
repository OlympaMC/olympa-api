package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.task.BungeeTaskManager;
import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.plugin.OlympaPluginInterface;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.core.bungee.datamanagment.AuthListener;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;

public class OlympaBungee extends Plugin implements LinkSpigotBungee, OlympaPluginInterface {

	private static OlympaBungee instance;

	public static OlympaBungee getInstance() {
		return instance;
	}

	protected BungeeCustomConfig defaultConfig;
	private BungeeTaskManager task;
	private ServerStatus status;

	@Override
	public void onDisable() {
		sendMessage("&4" + getDescription().getName() + "&c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		LinkSpigotBungee.Provider.link = this;
		instance = this;
		task = new BungeeTaskManager(this);
		status = ServerStatus.MAINTENANCE;
		OlympaPermission.registerPermissions(OlympaAPIPermissions.class);

		PluginManager pluginManager = getProxy().getPluginManager();
		pluginManager.registerListener(this, new AuthListener());
		sendMessage("&2" + getDescription().getName() + "&a (" + getDescription().getVersion() + ") est activé.");

	}

	@Override
	public String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	@Override
	public Connection getDatabase() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void launchAsync(Runnable run) {
		getTask().runTaskAsynchronously(run);

	}

	@Override
	public String getServerName() {
		return "bungee";
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getProxy().getConsole().sendMessage(TextComponent.fromLegacyText(String.format(ColorUtils.color(getPrefixConsole() + message), args)));

	}

	@Override
	public boolean isSpigot() {
		return false;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public ServerStatus getStatus() {
		return status;
	}

	@Override
	public OlympaTask getTask() {
		return task;
	}

	@Override
	public OlympaServer getOlympaServer() {
		// TODO Auto-generated method stub
		return null;
	}

}
