package fr.olympa.api.common.plugin;

import java.util.List;
import java.util.function.BiConsumer;

import fr.olympa.api.common.redis.ResourcePackHandler;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.api.spigot.frame.ImageFrameManager;
import fr.olympa.api.spigot.holograms.HologramsManager;
import fr.olympa.api.spigot.region.tracking.RegionManager;

public interface OlympaCoreInterface {

	RegionManager getRegionManager();

	HologramsManager getHologramsManager();

	ImageFrameManager getImageFrameManager();

	void registerPackListener(ResourcePackHandler packHandler);

	/**
	 * Execute le @param callback avec les dernières informations des serveurs spigot si possible. Si les données sont plus anciennes de 10 secondes,
	 * on demande au bungee, et lors qu'un recevera la réponse, @param callback sera exécuté une seconde fois.
	 * Tous les callback dans {@link fr.olympa.core.spigot.redis.receiver#BungeeServerInfoReceiver callbacksRegister} sont aussi executés.
	 */
	void retreiveMonitorInfos(BiConsumer<List<ServerInfoBasic>, Boolean> callback, boolean freshDoubleCallBack);

	void setOlympaServer(OlympaServer olympaServer);
}
