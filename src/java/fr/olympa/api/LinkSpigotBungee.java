package fr.olympa.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.google.gson.Gson;

import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.utils.Utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public interface LinkSpigotBungee {

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

	void sendMessage(String message, Object... args);

	boolean isSpigot();

	boolean isEnabled();

	ServerStatus getStatus();

	OlympaTask getTask();

	OlympaServer getOlympaServer();

	Gson getGson();

	List<String> getPlayersNames();

	static LinkSpigotBungee getInstance() {
		return Provider.link;
	}

	public static final class Provider {
		public static LinkSpigotBungee link;
	}

	boolean isServerName(String serverName);

	void setServerName(String serverName);

	void setStatus(ServerStatus status);

	void registerRedisSub(JedisPubSub sub, String channel);

	void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel);

	RedisConnection getRedisAccess();
}
