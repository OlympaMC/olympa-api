package fr.olympa.api;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Utils;

public interface LinkSpigotBungee {

	public static final class Provider {
		public static LinkSpigotBungee link;
	}

	long upTime = Utils.getCurrentTimeInSeconds();

	default String getUptime() {
		return Utils.timestampToDuration(LinkSpigotBungee.upTime);
	}

	Connection getDatabase() throws SQLException;

	void launchAsync(Runnable run);

	String getServerName();

	void sendMessage(String message, Object... args);

	boolean isSpigot();

	ServerStatus getStatus();

	default long getUptimeLong() {
		return upTime;
	}

	OlympaTask getTask();
}
