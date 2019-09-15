package fr.tristiisch.olympa.api.utils;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpigotUtils {

	public static List<String> color(final List<String> l) {
		return l.stream().map(s -> SpigotUtils.color(s)).collect(Collectors.toList());
	}

	public static String color(final String s) {
		return s != null ? ChatColor.translateAlternateColorCodes('&', s) : "";
	}

	public static boolean isSameLocation(final Location location1, final Location location2) {
		return location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY() && location1.getBlockZ() == location2.getBlockZ();
	}

	public static boolean isSamePlayer(final Player player, final Player target) {
		return player.getUniqueId().equals(target.getUniqueId());
	}

	public static boolean playerisIn(final Player player, final Location location) {
		final Location playerLocation = player.getLocation();
		return playerLocation.getBlockX() == location.getBlockX() && (playerLocation.getBlockY() == location.getBlockY() || playerLocation.getBlockY() + 1 == location.getBlockY()) && playerLocation.getBlockZ() == location.getBlockZ();
	}

}
