package fr.olympa.api.common.player;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.common.provider.OlympaPlayerObject;

public interface OlympaAccount {

	String REDIS_KEY = "player:";
	int DELAY_FOR_CACHE_PLAYER = 60;

	Map<UUID, OlympaPlayer> cache = new HashMap<>();
	OlympaPlayerProvider pluginPlayerProvider = OlympaPlayerObject::new;

	static Map<UUID, OlympaPlayer> getCache() {
		return cache;
	}

	OlympaPlayer createOlympaPlayer(String name, String ip);

	OlympaPlayer get() throws SQLException;

	void saveToRedis(OlympaPlayer olympaPlayer);

	OlympaPlayer getFromCache();

	void removeFromCache();

	OlympaPlayer getFromRedis();
}