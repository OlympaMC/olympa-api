package fr.tristiisch.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;

public class AccountProvider {

	private static Map<UUID, OlympaPlayer> cache = new HashMap<>();

	public static OlympaPlayer get(Player player) {
		return get(player.getUniqueId());
	}

	public static OlympaPlayer get(UUID uuid) {
		return cache.get(uuid);
	}

	public static OlympaPlayer getFromDatabase(String name) throws SQLException {
		return null;
	}

	public static OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		return null;
	}
}
