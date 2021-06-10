package fr.olympa.api.common.plugin;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public interface OlympaBungeeInterface {

	Configuration getConfig();

	void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel);

	BungeeCustomConfig getDefaultConfig();

}