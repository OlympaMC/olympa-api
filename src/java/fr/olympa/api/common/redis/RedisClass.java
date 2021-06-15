package fr.olympa.api.common.redis;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.common.redis.bungeesub.SpigotServerSwitchReceiver;
import fr.olympa.api.common.redis.spigotsub.BungeeServerInfoReceiver;
import fr.olympa.core.bungee.OlympaBungee;
import fr.olympa.core.spigot.OlympaCore;

public class RedisClass {

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static BungeeServerInfoReceiver SERVER_INFO;

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public static SpigotServerSwitchReceiver SERVER_SWITCH;

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	private static void register(LinkSpigotBungee core) {
		SERVER_INFO = new BungeeServerInfoReceiver(core);
		SERVER_SWITCH = new SpigotServerSwitchReceiver(core);
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static void registerSpigotSubChannels(OlympaCore core) {
		register(core);
		SERVER_INFO.register(core.getRedisAccess().connect());
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public static void registerBungeeSubChannels(OlympaBungee core) {
		register(core);
		//		SERVER_SWITCH.register(core.getRedisAccess().connect());
	}

	private RedisClass() {}
}
