package fr.olympa.api.server;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.plugin.DebugPluginsInit;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class ServerDebugInit extends ServerDebug {

	public ServerDebugInit(OlympaCore core) {
		super();
		name = core.getServerName();
		status = core.getStatus();
		uptime = Utils.tsToShortDur(LinkSpigotBungee.Provider.link.getUptimeLong());
		bukkitVersion = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		tps = TPS.getTPS();
		firstVersionMinecraft = core.getFirstVersion();
		lastVersionMinecraft = core.getLastVersion();
		try {
			databaseConnected = core.getDatabase() != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (cachePlugins == null)
			cachePlugins = Arrays.stream(core.getServer().getPluginManager().getPlugins()).map(DebugPluginsInit::new).collect(Collectors.toList());
		plugins = cachePlugins;
	}
}
