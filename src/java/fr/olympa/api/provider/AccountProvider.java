package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaAccount;

public class AccountProvider implements OlympaAccount {

	public static Map<UUID, OlympaPlayer> cache = new HashMap<>();

	public static OlympaPlayer get(UUID uuid) {
		return cache.get(uuid);
	}

	public static OlympaPlayer getFromDatabase(String name) throws SQLException {
		return null;
	}

	public static OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		return null;
	}

	public AccountProvider(UUID uuid) {
	}

	@Override
	public OlympaPlayer createOlympaPlayer(String name, String ip) {
		return null;
	}

	@Override
	public OlympaPlayer get() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveToDb(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToRedis(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}
}
