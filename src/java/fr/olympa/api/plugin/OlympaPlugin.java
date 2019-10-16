package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.OlympaCore;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.task.TaskManager;
import fr.olympa.api.utils.SpigotUtils;

public abstract class OlympaPlugin extends JavaPlugin {

	protected static OlympaPlugin instance;
	private TaskManager task;
	private CustomConfig config;
	protected DbConnection database = null;

	protected void disable() {
		if (this.database != null) {
			this.database.close();
		}
	}

	protected void enable(OlympaPlugin plugin) {
		instance = plugin;
		this.task = new TaskManager(this);

		this.config = new CustomConfig(this, "config");
		if (this.config.hasResource()) {
			this.config.load();
		} else {
			this.config = null;
			return;
		}

		DbCredentials dbcredentials = new DbCredentials(this.config);
		if (dbcredentials.getUser() == null) {
			return;
		}
		this.database = new DbConnection(dbcredentials);
		if (this.database.connect()) {
			OlympaCore.getInstance().sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie");
		} else {
			OlympaCore.getInstance().sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible");
		}
	}

	@Override
	public CustomConfig getConfig() {
		return this.config;
	}

	public Connection getDatabase() throws SQLException {
		return this.database.getConnection();
	}

	private String getPrefixConsole() {
		return "&f[&6" + this.getDescription().getName() + "&f] &e";
	}

	public TaskManager getTask() {
		return this.task;
	}

	public void sendMessage(final String message) {
		this.getServer().getConsoleSender().sendMessage(SpigotUtils.color(this.getPrefixConsole() + message));
	}
}
