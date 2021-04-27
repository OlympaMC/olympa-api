package fr.olympa.api.utils.machine;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import com.google.gson.Gson;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.TPS;
import fr.olympa.core.spigot.OlympaCore;

public class ServerDebugInfo extends MachineInfo {

	public static ServerDebugInfo fromJson(String string) {
		return new Gson().fromJson(string, ServerDebugInfo.class);
	}

	static List<DebugPlugin> cachePlugins = null;

	String name;
	ServerStatus status;
	String uptime;
	float tps;
	List<DebugPlugin> plugins;
	String firstVersionMinecraft;
	String lastVersionMinecraft;
	String bukkitVersion;
	boolean hasConfig;
	boolean databaseConnected;
	boolean redisConnected;

	public ServerDebugInfo(OlympaCore core) {
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
			cachePlugins = Arrays.stream(core.getServer().getPluginManager().getPlugins()).map(DebugPlugin::new).collect(Collectors.toList());
		plugins = cachePlugins;
	}

	public String getName() {
		return name;
	}

	public ServerStatus getStatus() {
		return status;
	}

	public float getTps() {
		return tps;
	}

	public List<DebugPlugin> getPlugins() {
		return plugins;
	}

	public String getFirstVersionMinecraft() {
		return firstVersionMinecraft;
	}

	public String getLastVersionMinecraft() {
		return lastVersionMinecraft;
	}

	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
