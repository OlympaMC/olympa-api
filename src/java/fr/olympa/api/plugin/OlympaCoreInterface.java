package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Consumer;

import fr.olympa.api.frame.ImageFrameManager;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.region.tracking.RegionManager;
import fr.olympa.api.server.MonitorInfo;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;

public interface OlympaCoreInterface {

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

	Connection getDatabase() throws SQLException;

	List<MonitorInfo> getMonitorInfos();

	void retreiveMonitorInfos(Consumer<List<MonitorInfo>> callback);

}
