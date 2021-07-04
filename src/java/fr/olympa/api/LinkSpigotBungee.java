package fr.olympa.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.jetbrains.annotations.NotNull;

import com.google.gson.Gson;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsGlobal;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerStatus;
import fr.olympa.api.common.task.OlympaTask;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.api.spigot.version.VersionHandler;
import fr.olympa.api.utils.Utils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public interface LinkSpigotBungee<P> {

	long upTime = Utils.getCurrentTimeInSeconds();

	default String getUptime() {
		return Utils.timestampToDuration(LinkSpigotBungee.upTime);
	}

	default long getUptimeLong() {
		return upTime;
	}
	
	default void setupGlobalTasks() {
		OlympaAPIPermissionsGlobal.setServerJoinPermissions();
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

	static LinkSpigotBungee<?> getInstance() {
		return Provider.link;
	}
	
	static void setInstance(LinkSpigotBungee<?> link) {
		Provider.link = link;
	}

	public static final class Provider {
		/**
		 * @deprecated use {@link LinkSpigotBungee#getInstance()}
		 */
		@Deprecated
		public static LinkSpigotBungee<?> link;
	}

	boolean isServerName(String serverName);

	void setServerName(String serverName);

	void setStatus(ServerStatus status);

	void registerRedisSub(JedisPubSub sub, String channel);

	void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel);

	RedisConnection getRedisAccess();

	boolean isRedisConnected();

	boolean isDatabaseConnected();

	VersionHandler<?> getVersionHandler();

	List<ProtocolAPI> getProtocols();

	@NotNull
	Collection<? extends P> getPlayers();

	P getPlayer(String playerName);

	P getPlayer(UUID uuid);
}
