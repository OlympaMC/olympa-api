package fr.olympa.api.common.server;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.plugin.PluginInfoSpigot;
import fr.olympa.api.common.sort.Sorting;
import fr.olympa.api.spigot.utils.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class DebugServerSpigot extends ServerInfoAdvanced {

	public DebugServerSpigot(OlympaCore core) {
		super();
		name = core.getServerName();
		status = core.getStatus();
		uptime = LinkSpigotBungee.Provider.link.getUptimeLong();
		bukkitVersion = Bukkit.getBukkitVersion().replace("-SNAPSHOT", "");
		tps = TPS.getTPS();
		firstVersionMinecraft = core.getFirstVersion();
		lastVersionMinecraft = core.getLastVersion();
		try {
			databaseConnected = core.getDatabase() != null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if (ownPlugins == null)
			updatePlugins(core);
		plugins = ownPlugins;
	}

	public void updatePlugins(OlympaCore core) {
		ownPlugins = Arrays.stream(core.getServer().getPluginManager().getPlugins()).map(PluginInfoSpigot::new)
				.sorted(new Sorting<>(dp -> dp.getName().startsWith("Olympa") ? 0l : 1l,
						dp -> dp.getAuthors().stream().anyMatch(author -> author.equals("SkytAsul") || author.equals("Tristiisch") || author.equals("Bullobily")) ? 0l : 1l))
				.collect(Collectors.toList());

	}
}
