package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.frame.ImageFrameManager;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;

public interface OlympaCoreInterface {
	
	Connection getDatabase() throws SQLException;
	
	INametagApi getNameTagApi();
	
	RegionManager getRegionManager();
	
	HologramsManager getHologramsManager();

	ImageFrameManager getImageFrameManager();
	
	String getServerName();
	
	boolean isServerName(String serverName);
	
	ServerStatus getStatus();
	
	void setServerName(String serverName);
	
	void setStatus(ServerStatus status);
	
	OlympaServer getOlympaServer();
	
	void setOlympaServer(OlympaServer olympaServer);
	
}
