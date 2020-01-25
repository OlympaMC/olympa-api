package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Utils;

public interface OlympaPluginInterface {

	long upTime = Utils.getCurrentTimeInSeconds();

	Connection getDatabase() throws SQLException;

	String getPrefixConsole();

	/*
	 * Uniquement via OlympaCore#getInstance() ou OlympaBungee#getInstance()
	 */
	MaintenanceStatus getStatus();

	OlympaTask getTask();

	default String getUptime() {
		return Utils.timestampToDuration(OlympaPluginInterface.upTime);
	}

	default long getUptimeLong() {
		return upTime;
	}

	void sendMessage(String message);

	/*
	 * Uniquement via OlympaCore#getInstance() ou OlympaBungee#getInstance()
	 */
	void setStatus(MaintenanceStatus status);
}