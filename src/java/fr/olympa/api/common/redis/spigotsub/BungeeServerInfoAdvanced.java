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
import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisClass;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.redis.RedisSubChannel;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.core.bungee.OlympaBungee;
import redis.clients.jedis.Jedis;

public class BungeeServerInfoAdvanced extends RedisSubChannel {

	private List<Consumer<List<ServerInfoAdvanced>>> callbacksRegister = new ArrayList<>();

	public List<Consumer<List<ServerInfoAdvanced>>> getCallbacksRegister() {
		return callbacksRegister;
	}

	public boolean registerCallback(Consumer<List<ServerInfoAdvanced>> callback) {
		return callbacksRegister.add(callback);
	}

	public BungeeServerInfoAdvanced(LinkSpigotBungee<?> core, RedisChannel channel) {
		super(core, channel);
		canSendFromSpigot = false;
		canSendFromBungee = true;
		canReceiveOnSpigot = true;
		canReceiveOnBungee = false;
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public boolean sendServerInfos() {
		return sendServerInfos(OlympaBungee.getInstance().getMonitorServers());
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public boolean sendServerInfos(Collection<ServerInfoAdvanced> serveursInfoBasic) {
		if (serveursInfoBasic.isEmpty())
			return false;
		RedisConnection redis = getRedisAccess();
		try (Jedis jedis = redis.connect()) {
			jedis.publish(getChannelName(), serveursInfoBasic.stream().map(LinkSpigotBungee.getInstance().getGson()::toJson).collect(Collectors.joining("\n")));
		}
		redis.disconnect();
		return true;
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] serversInfoJson = message.split("\\n");
		List<ServerInfoAdvanced> newMonitorInfos = new ArrayList<>();
		for (String s : serversInfoJson) {
			if (OlympaModule.DEBUG)
				LinkSpigotBungee.getInstance().sendMessage("&7Debug les infos de &e%s&7 ont été reçu", s);
			ServerInfoAdvanced monitorInfo = LinkSpigotBungee.getInstance().getGson().fromJson(s, ServerInfoAdvanced.class);
			newMonitorInfos.add(monitorInfo);
		}
		callbacksRegister.forEach(c -> c.accept(newMonitorInfos));
		List<BiConsumer<List<ServerInfoAdvanced>, Boolean>> askServerInfo = RedisClass.ASK_SERVER_INFO.getAskServerInfo();
		askServerInfo.forEach(c -> c.accept(newMonitorInfos, false));
		askServerInfo.clear();
	}
}
