package fr.olympa.api.common.redis;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.common.redis.bungeesub.SpigotAskServerInfo;
import fr.olympa.api.common.redis.bungeesub.SpigotServerSwitch;
import fr.olympa.api.common.redis.spigotsub.BungeeServerInfo;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.OlympaCore;

public class RedisClass {

	public static BungeeServerInfo SERVER_INFO;

	public static SpigotServerSwitch SERVER_SWITCH;
	public static SpigotAskServerInfo ASK_SERVER_INFO;

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	private static void register(LinkSpigotBungee core) {
		ASK_SERVER_INFO = new SpigotAskServerInfo(core, RedisChannel.SPIGOT_ASK_SERVERINFO);
		SERVER_INFO = new BungeeServerInfo(core, RedisChannel.BUNGEE_SEND_SERVERSINFOS2);
		SERVER_SWITCH = new SpigotServerSwitch(core, RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER);
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static void registerSpigotSubChannels(OlympaCore core) {
		register(core);
		RedisConnection redisAcccess = core.getRedisAccess();
		SERVER_INFO.register(redisAcccess.connect());
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public static void registerBungeeSubChannels(OlympaBungee core) {
		register(core);
		RedisConnection redisAcccess = core.getRedisAccess();
		SERVER_SWITCH.register(redisAcccess.connect());
		ASK_SERVER_INFO.register(redisAcccess.connect());
	}

	private RedisClass() {}
}
