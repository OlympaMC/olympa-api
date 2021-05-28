package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.entity.Player;

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

	void usesPack(Player p);

	/**
	 * Execute le @param callback avec les dernières informations des serveurs spigot si possible. Si les données sont plus anciennes de 10 secondes,
	 * on demande au bungee, et lors qu'un recevera la réponse, @param callback sera exécuté une seconde fois.
	 * Tous les callback dans {@link fr.olympa.core.spigot.redis.receiver#BungeeServerInfoReceiver callbacksRegister} sont aussi executés.
	 */
	void retreiveMonitorInfos(BiConsumer<List<MonitorInfo>, Boolean> callback, boolean freshDoubleCallBack);

}
