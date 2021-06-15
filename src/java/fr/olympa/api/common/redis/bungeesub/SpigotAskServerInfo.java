package fr.olympa.api.common.redis.bungeesub;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.redis.RedisSubChannel;
import fr.olympa.api.common.server.ServerInfoBasic;
import fr.olympa.core.spigot.OlympaCore;
import redis.clients.jedis.Jedis;

public class SpigotAskServerInfo extends RedisSubChannel {

	private List<BiConsumer<List<ServerInfoBasic>, Boolean>> askServerInfo = new ArrayList<>();

	public SpigotAskServerInfo(LinkSpigotBungee core, RedisChannel channel) {
		super(core, channel);
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

	public List<BiConsumer<List<ServerInfoBasic>, Boolean>> getAskServerInfo() {
		return askServerInfo;
	}

}
