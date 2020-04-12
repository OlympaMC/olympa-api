package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.objects.OlympaPlayerProvider;
import fr.olympa.api.permission.OlympaAccount;

public class AccountProvider implements OlympaAccount {

	public static Map<UUID, OlympaPlayer> cache = new HashMap<>();
	public static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();
	private UUID uuid;

	public static <T extends OlympaPlayer> T get(UUID uuid) {
		return (T) cache.get(uuid);
	}

	public static OlympaPlayerInformations getPlayerInformations(long id) {
		return (OlympaPlayerInformations) cachedInformations.get(id);
	}

	public static OlympaPlayer getFromDatabase(String name) throws SQLException {
		return null;
	}

	public static OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		return null;
	}

	public static void setPlayerProvider(Class<? extends OlympaPlayerObject> playerClass, OlympaPlayerProvider supplier, String pluginName, Map<String, String> columns) {

	}

	public AccountProvider(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public OlympaPlayer createOlympaPlayer(String name, String ip) {
		return new OlympaPlayerObject(uuid, name, ip, cache.size());
	}

	@Override
	public OlympaPlayer get() throws SQLException {
		return AccountProvider.get(uuid);
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
