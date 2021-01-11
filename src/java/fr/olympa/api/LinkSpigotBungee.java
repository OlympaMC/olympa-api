package fr.olympa.api;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public interface LinkSpigotBungee {

	public static final class Provider {
		public static LinkSpigotBungee link;
	}

	long upTime = Utils.getCurrentTimeInSeconds();

	default String getUptime() {
		return Utils.timestampToDuration(LinkSpigotBungee.upTime);
	}

	Connection getDatabase() throws SQLException;

	void launchAsync(Runnable run);

	String getServerName();

	void sendMessage(String message, Object... args);

	boolean isSpigot();

	ServerStatus getStatus();

	default long getUptimeLong() {
		return upTime;
	}

	OlympaTask getTask();

	OlympaServer getOlympaServer();

	static List<String> getPlayersNames() {
		if (Provider.link.isSpigot())
			return Bukkit.getServer().getOnlinePlayers().stream().map(Player::getDisplayName).collect(Collectors.toList());
		else
			return ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getDisplayName).collect(Collectors.toList());
	}
}
