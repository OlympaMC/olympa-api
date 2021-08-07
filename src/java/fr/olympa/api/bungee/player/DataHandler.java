package fr.olympa.api.bungee.player;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import fr.olympa.api.bungee.utils.BungeeUtils;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class DataHandler {

	private static Set<CachePlayer> players = new HashSet<>();

	public static void addPlayer(CachePlayer player) {
		CachePlayer cache = get(player.getName());
		boolean isCacheNull = cache == null;
		boolean isNotSameObject = !isCacheNull && !cache.equals(player);
		if (isNotSameObject)
			removePlayer(cache);
		if (isCacheNull || isNotSameObject)
			DataHandler.players.add(player);
		OlympaBungee bungee = OlympaBungee.getInstance();
		bungee.getTask().runTaskLater("login_" + player.getName(), () -> {
			ProxiedPlayer pd = bungee.getProxy().getPlayer(player.getName());
			if (pd != null)
				pd.disconnect(BungeeUtils.connectScreen("Tu as mis trop de temps à saisir ton mot de passe."));
		}, 120, TimeUnit.SECONDS);
	}

	public static synchronized CachePlayer get(String name) {
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
		if (player == null)
			return;
		OlympaBungee bungee = OlympaBungee.getInstance();
		bungee.getTask().cancelTaskByName("login_" + player.getName());
		DataHandler.players.remove(player);
	}

	public static void removePlayer(String name) {
		removePlayer(get(name));
	}
}
