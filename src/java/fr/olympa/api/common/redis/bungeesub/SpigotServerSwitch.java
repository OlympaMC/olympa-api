package fr.olympa.api.common.redis.bungeesub;

import org.bukkit.entity.Player;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.redis.RedisChannel;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.redis.RedisSubChannel;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.Callback;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import redis.clients.jedis.Jedis;

public class SpigotServerSwitch extends RedisSubChannel {

	public SpigotServerSwitch(LinkSpigotBungee core, RedisChannel channel) {
		super(core, channel);
	}

	public void sendServerSwitch(Player p, OlympaServer server) {
		RedisConnection redis = getRedisAccess();
		try (Jedis jedis = redis.connect()) {
			jedis.publish(getChannelName(), p.getName() + ":" + server.name());
		}
		redis.disconnect();
	}

	public void sendServerSwitch(Player p, String serverName) {
		RedisConnection redis = getRedisAccess();
		try (Jedis jedis = redis.connect()) {
			jedis.publish(RedisChannel.SPIGOT_PLAYER_SWITCH_SERVER2.name(), p.getName() + ":" + serverName);
		}
		redis.disconnect();
	}

	@Override
	public void onMessage(String channel, String message) {
		super.onMessage(channel, message);
		String[] args = message.split(":");
		ServerInfo target = null;
		OlympaBungee instance = OlympaBungee.getInstance();
		ProxiedPlayer player = OlympaBungee.getInstance().getProxy().getPlayer(args[0]);
		String serverName = null;
		if (args.length >= 2 && !args[1].isBlank() && !args[1].equals("null")) {
			target = instance.getProxy().getServersCopy().get(args[1]);
			if (target != null)
				serverName = Utils.capitalize(target.getName());
		}

		if (args.length >= 3 && !args[2].isBlank() && !args[2].equals("null")) {
			OlympaServer olympaServer = OlympaServer.valueOf(args[2]);
			if (olympaServer != null) {
				//				ServersConnection.tryConnect(player, server, false);
				serverName = Utils.capitalize(olympaServer.getNameCaps());
				System.out.println(String.format("[REDIS] Demande de serveur switch %s sur le serv %s.", player.getName(), serverName));
			}
		}

		if (target == null || serverName == null)
			return;

		if (player.getServer() != null && player.getServer().getInfo().equals(target)) {
			player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Tu es déjà au §2%s§a. ", serverName));
			return;
		}
		String serverNameFinal = serverName;
		Callback<Boolean> callback = (result, error) -> {
			if (result)
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_GOOD, "Connexion au serveur %s établie !", serverNameFinal));
			//			else if (error == null)
			//				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Tu es déjà au %s !", serverName));
			else if (error != null)
				player.sendMessage(TxtComponentBuilder.of(Prefix.DEFAULT_BAD, "Echec de la connexion au serveur &4%s&c: &4%s&c. ", serverNameFinal, error.getMessage()));
		};
		System.out.println(String.format("[REDIS] Demande de serveur switch %s sur le serv %s.", player.getName(), serverName));
		//		player.connect(server, callback, false, Reason.PLUGIN_MESSAGE, 10000);
	}

}
