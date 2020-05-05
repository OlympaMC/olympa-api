package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.region.RegionManager;
import fr.olympa.api.scoreboard.tab.INametagApi;

public interface OlympaCoreInterface {

	Connection getDatabase() throws SQLException;

	INametagApi getNameTagApi();

	RegionManager getRegionManager();

	String getServerName();

	MaintenanceStatus getStatus();

	void setServerName(String serverName);

	void setStatus(MaintenanceStatus status);
}
