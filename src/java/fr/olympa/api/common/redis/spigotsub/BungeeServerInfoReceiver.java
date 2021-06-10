package fr.olympa.api.common.redis.spigotsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.plugin.OlympaCoreInterface;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisSubChannel;
import fr.olympa.api.common.server.ServerInfoBasic;
import redis.clients.jedis.Jedis;

public class BungeeServerInfoReceiver extends RedisSubChannel {

	private static List<Consumer<List<ServerInfoBasic>>> callbacksRegister = new ArrayList<>();

	public static List<Consumer<List<ServerInfoBasic>>> getCallbacksRegister() {
		return callbacksRegister;
	}

	public static boolean registerCallback(Consumer<List<ServerInfoBasic>> callback) {
		return callbacksRegister.add(callback);
	}

	public BungeeServerInfoReceiver(OlympaCoreInterface core) {
		super(core, RedisChannel.BUNGEE_SEND_SERVERSINFOS2);
		canSendFromSpigot = false;
		canSendFromBungee = true;
		canReceiveOnSpigot = true;
		canReceiveOnBungee = false;
	}

	//	public boolean sendServerInfos() {
	//		return sendServerInfos(MonitorServers.getServers());
	//	}

	public boolean sendServerInfos(Collection<ServerInfoBasic> serveursInfoBasic) {
		if (serveursInfoBasic.isEmpty())
			return false;
		try (Jedis jedis = getRedisAccess().connect()) {
			jedis.publish(RedisChannel.BUNGEE_SEND_SERVERSINFOS2.name(), serveursInfoBasic.stream().map(LinkSpigotBungee.getInstance().getGson()::toJson).collect(Collectors.joining("\n")));
		}
		getRedisAccess().disconnect();
		return true;
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
		//		RedisSpigotSend.askServerInfo.forEach(c -> c.accept(newMonitorInfos, false));
		//		RedisSpigotSend.askServerInfo.clear();
	}
}
