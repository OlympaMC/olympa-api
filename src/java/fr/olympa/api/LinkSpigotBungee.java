package fr.olympa.api;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.Utils;

public interface LinkSpigotBungee {

	public static final class Provider {
		public static LinkSpigotBungee link;
	}

	long upTime = Utils.getCurrentTimeInSeconds();

	default String getUptime() {
		return Utils.timestampToDuration(LinkSpigotBungee.upTime);
	}

	default long getUptimeLong() {
		return upTime;
	}

	Connection getDatabase() throws SQLException;

	void launchAsync(Runnable run);

	String getServerName();

	ServerStatus getStatus();
}