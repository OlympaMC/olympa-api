package fr.olympa.api.common.server;

import java.util.ArrayList;
import java.util.Collection;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.utils.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class ServerInfoAdvancedSpigot extends ServerInfoAdvanced {

	public ServerInfoAdvancedSpigot() {
		super();
	}

	public ServerInfoAdvancedSpigot(OlympaCore core) {
		super(core);
		Server server = core.getServer();
		maxPlayers = server.getMaxPlayers();
		Collection<? extends Player> playersSpigot = server.getOnlinePlayers();
		onlinePlayers = playersSpigot.size();
		players = new ArrayList<>();
		versions = core.getProtocols();
		server.getOnlinePlayers().forEach(player -> {
			players.add(AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId()));
		});
		serverFrameworkVersion = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		tps = TPS.getTPS();
	}
}
