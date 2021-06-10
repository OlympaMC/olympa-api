package fr.olympa.api.common.server;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerInfoAdvancedBungee extends ServerInfoAdvanced {

	public ServerInfoAdvancedBungee() {
		super();
	}

	public ServerInfoAdvancedBungee(OlympaBungee core) {
		ProxyServer server = core.getProxy();
		maxPlayers = server.getOnlineCount();
		Collection<ProxiedPlayer> playersSpigot = server.getPlayers();
		onlinePlayers = playersSpigot.size();

		players = new ArrayList<>();
		server.getPlayers().forEach(player -> {
			players.add(AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId()));
		});
		name = core.getServerName();
		olympaServer = core.getOlympaServer();
		status = core.getStatus();
		uptime = LinkSpigotBungee.Provider.link.getUptimeLong();
		serverFrameworkVersion = core.getProxy().getVersion();
		Entry<String, String> bungeeRangeVersion = ProtocolAPI.getVersionsRangeBungee();
		firstVersionMinecraft = bungeeRangeVersion.getKey();
		lastVersionMinecraft = bungeeRangeVersion.getValue();
		try {
			databaseConnected = core.getDatabase() != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		plugins = getCachePlugins();

	}
}
