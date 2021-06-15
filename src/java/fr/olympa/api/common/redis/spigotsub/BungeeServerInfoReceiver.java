package fr.olympa.api.common.redis.spigotsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.redis.RedisSubChannel;
import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class BungeeServerInfoReceiver extends RedisSubChannel {

	private List<BiConsumer<List<ServerInfoBasic>, Boolean>> askServerInfo = new ArrayList<>();
	private List<Consumer<List<ServerInfoBasic>>> callbacksRegister = new ArrayList<>();

	public List<Consumer<List<ServerInfoBasic>>> getCallbacksRegister() {
		return callbacksRegister;
	}

	public boolean registerCallback(Consumer<List<ServerInfoBasic>> callback) {
		return callbacksRegister.add(callback);
	}

	public BungeeServerInfoReceiver(LinkSpigotBungee core) {
		super(core, RedisChannel.BUNGEE_SEND_SERVERSINFOS2);
		canSendFromSpigot = false;
		canSendFromBungee = true;
		canReceiveOnSpigot = true;
		canReceiveOnBungee = false;
	}

	//	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	//	public boolean sendServerInfos() {
	//		return sendServerInfos(MonitorServers.getServers());
	//	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public boolean sendServerInfos(Collection<ServerInfoBasic> serveursInfoBasic) {
		if (serveursInfoBasic.isEmpty())
			return false;
		RedisConnection redis = getRedisAccess();
		try (Jedis jedis = redis.connect()) {
			jedis.publish(RedisChannel.BUNGEE_SEND_SERVERSINFOS2.name(), serveursInfoBasic.stream().map(LinkSpigotBungee.getInstance().getGson()::toJson).collect(Collectors.joining("\n")));
		}
		redis.disconnect();
		return true;
	}

	/**
	 * Déclanche @see fr.olympa.api.spigot.customevents.MonitorServerInfoReceiveEvent
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public void askServerInfo(BiConsumer<List<ServerInfoBasic>, Boolean> callback) {
		if (callback != null)
			askServerInfo.add(callback);
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			RedisConnection redis = getRedisAccess();
			try (Jedis jedis = redis.connect()) {
				String serverName = OlympaCore.getInstance().getServerName();
				jedis.publish(RedisChannel.SPIGOT_ASK_SERVERINFO.name(), serverName);
			}
			redis.disconnect();
		});
	}

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] serversInfoJson = message.split("\\n");
		List<ServerInfoBasic> newMonitorInfos = new ArrayList<>();
		for (String s : serversInfoJson) {
			ServerInfoBasic monitorInfo = LinkSpigotBungee.getInstance().getGson().fromJson(s, ServerInfoBasic.class);
			newMonitorInfos.add(monitorInfo);
		}
		callbacksRegister.forEach(c -> c.accept(newMonitorInfos));
		askServerInfo.forEach(c -> c.accept(newMonitorInfos, false));
		askServerInfo.clear();
	}
}
