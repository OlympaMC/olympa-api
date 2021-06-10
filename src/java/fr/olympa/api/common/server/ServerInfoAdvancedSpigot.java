package fr.olympa.api.common.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.utils.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class ServerInfoAdvancedSpigot extends ServerInfoAdvanced {

	public ServerInfoAdvancedSpigot() {
		super();
	}

	public ServerInfoAdvancedSpigot(OlympaCore core) {
		Server server = core.getServer();
		maxPlayers = server.getMaxPlayers();
		Collection<? extends Player> playersSpigot = server.getOnlinePlayers();
		onlinePlayers = playersSpigot.size();

		players = new ArrayList<>();
		server.getOnlinePlayers().forEach(player -> {
			players.add(AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId()));
		});
		name = core.getServerName();
		olympaServer = core.getOlympaServer();
		status = core.getStatus();
		uptime = LinkSpigotBungee.Provider.link.getUptimeLong();
		serverFrameworkVersion = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		tps = TPS.getTPS();
		firstVersionMinecraft = core.getFirstVersion();
		lastVersionMinecraft = core.getLastVersion();
		try {
			databaseConnected = core.getDatabase() != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		plugins = getCachePlugins();
	}
}
