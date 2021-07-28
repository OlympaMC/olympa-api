package fr.olympa.api.common.server;

import java.util.Collection;
import java.util.stream.Collectors;

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
		this(core, true);
	}

	public ServerInfoAdvancedSpigot(OlympaCore core, boolean savePlayers) {
		super(core);
		Server server = core.getServer();
		maxPlayers = server.getMaxPlayers();
		Collection<? extends Player> playersSpigot = server.getOnlinePlayers();
		onlinePlayers = playersSpigot.size();
		if (savePlayers) players = playersSpigot.stream()
				.map(player -> AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId()))
				.collect(Collectors.toList());
		serverFrameworkVersion = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		//tps = TPS.getTPS();
		tpsArray = TPS.getDoubleTPS();
	}
	
	@Override
	public boolean isSpigot() {
		return true;
	}
}
