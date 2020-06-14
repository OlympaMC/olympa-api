package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.ServerStatus;

public interface OlympaCoreInterface {

	Connection getDatabase() throws SQLException;

	INametagApi getNameTagApi();

	RegionManager getRegionManager();

	String getServerName();

	ServerStatus getStatus();

	void setServerName(String serverName);

	void setStatus(ServerStatus status);
}
