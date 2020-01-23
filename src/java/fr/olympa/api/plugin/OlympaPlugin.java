package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.sql.DbConnection;
import fr.olympa.api.sql.DbCredentials;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.task.TaskManager;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;

public abstract class OlympaPlugin extends JavaPlugin {

	private OlympaTask task;
	private CustomConfig config;
	protected DbConnection database = null;
	protected long uptime = Utils.getCurrentTimeInSeconds();
	MaintenanceStatus status = MaintenanceStatus.DEV;

	protected void disable() {
		if (this.database != null) {
			this.database.close();
		}
	}

	public void enable() {
		this.task = new TaskManager(this);

		this.config = new CustomConfig(this, "config");
		if (this.config.hasResource() || this.config.getFile().exists()) {
			this.config.load();
			this.config.saveIfNotExists();

			String statusString = this.config.getString("status");
			if (statusString != null && !statusString.isEmpty()) {
				MaintenanceStatus status2 = MaintenanceStatus.get(statusString);
				if (status2 != null) {
					this.status = status2;
				}
			}

			this.setupDatabase();
		} else {
			this.config = null;
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

	public MaintenanceStatus getStatus() {
		return this.status;
	}

	public OlympaTask getTask() {
		return this.task;
	}

	public String getUptime() {
		return Utils.timestampToDuration(this.uptime);
	}

	public long getUptimeLong() {
		return this.uptime;
	}

	public void sendMessage(final String message) {
		this.getServer().getConsoleSender().sendMessage(SpigotUtils.color(this.getPrefixConsole() + message));
	}

	public void setStatus(MaintenanceStatus status) {
		this.status = status;
	}

	private void setupDatabase() {
		DbCredentials dbcredentials = new DbCredentials(this.config);
		if (dbcredentials.getUser() == null) {
			return;
		}
		this.database = new DbConnection(dbcredentials);
		if (this.database.connect()) {
			this.sendMessage("&aConnexion à la base de donnée &2" + dbcredentials.getDatabase() + "&a établie.");
		} else {
			this.sendMessage("&cConnexion à la base de donnée &4" + dbcredentials.getDatabase() + "&c impossible.");
		}
	}
}
