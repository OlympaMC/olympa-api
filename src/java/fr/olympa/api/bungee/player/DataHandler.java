package fr.olympa.api.bungee.player;

import java.util.HashSet;
import java.util.Set;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class DataHandler {

	private static Set<CachePlayer> players = new HashSet<>();

	public static void addPlayer(CachePlayer player) {
		CachePlayer cache = get(player.getName());
		boolean isCacheNull = cache == null;
		boolean isNotSameObject = !isCacheNull && !cache.equals(player);
		if (isCacheNull || isNotSameObject)
			DataHandler.players.add(player);
		if (isNotSameObject)
			removePlayer(cache);
	}

	public static CachePlayer get(String name) {
		return players.stream().filter(p -> p.getName() != null && name.equals(p.getName())).findFirst().orElse(null);
	}

	public static Set<CachePlayer> getPlayers() {
		return players;
	}

	public static boolean isUnlogged(ProxiedPlayer player) {
		return isUnlogged(player.getName());
	}

	public static boolean isUnlogged(String name) {
		return players.stream().anyMatch(p -> p.getName() != null && name.equals(p.getName()));
	}

	public static void removePlayer(CachePlayer player) {
		if (player != null)
			DataHandler.players.remove(player);
	}

	public static void removePlayer(String name) {
		removePlayer(get(name));
	}
}
