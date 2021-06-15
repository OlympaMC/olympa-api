package fr.olympa.api.common.plugin;

import java.util.Collection;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.common.server.ServerInfoBasic;
import net.md_5.bungee.config.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public interface OlympaBungeeInterface {

	Configuration getConfig();

	void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel);

	BungeeCustomConfig getDefaultConfig();

	Collection<ServerInfoBasic> getMonitorServers();

}