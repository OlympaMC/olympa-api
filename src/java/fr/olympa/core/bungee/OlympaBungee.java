package fr.olympa.core.bungee;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

import com.google.gson.Gson;

import fr.olympa.api.bungee.plugin.OlympaBungeeCore;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.core.bungee.datamanagment.AuthListener;
import net.md_5.bungee.api.plugin.PluginManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

public class OlympaBungee extends OlympaBungeeCore {

	private static OlympaBungee instance;

	public static OlympaBungee getInstance() {
		return instance;
	}

	@Override
	public void onLoad() {
		super.onLoad();
		instance = this;
	}

	@Override
	public void onDisable() {
		super.onDisable();
		sendMessage("&4" + getDescription().getName() + "&c (" + getDescription().getVersion() + ") est désactivé.");
	}

	@Override
	public void onEnable() {
		super.onEnable();
		
		PluginManager pluginManager = getProxy().getPluginManager();
		pluginManager.registerListener(this, new AuthListener());
		sendMessage("&2" + getDescription().getName() + "&a (" + getDescription().getVersion() + ") est activé.");

	}

	@Override
	public Connection getDatabase() throws SQLException {
		return null;
	}

	@Override
	public Gson getGson() {
		return new Gson();
	}

	@Override
	public Collection<ServerInfoAdvanced> getMonitorServers() {
		return null;
	}

	@Override
	public void registerRedisSub(JedisPubSub sub, String channel) {
		// TODO Auto-generated method stub

	}

	@Override
	public void registerRedisSub(Jedis jedis, JedisPubSub sub, String channel) {
		Thread t = new Thread(() -> {
			jedis.subscribe(sub, channel);
			jedis.disconnect();
		}, "Redis sub " + channel);
		Thread.UncaughtExceptionHandler h = (th, ex) -> {
			ex.printStackTrace();
			if (redisAccess != null)
				registerRedisSub(redisAccess.connect(), sub, channel);
		};
		t.setUncaughtExceptionHandler(h);
		t.start();

	}
}
