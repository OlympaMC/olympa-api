package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.player.OlympaAccount;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.player.OlympaPlayerProvider;

public class AccountProvider implements OlympaAccount {

	public static Map<UUID, OlympaPlayer> cache = new HashMap<>();
	public static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();
	private static OlympaPlayerProvider provider = OlympaPlayerObject::new;

	public static <T extends OlympaPlayer> T get(UUID uuid) {
		return (T) cache.get(uuid);
	}

	public static OlympaPlayerInformations getPlayerInformations(long id) {
		return (OlympaPlayerInformations) cachedInformations.get(id);
	}

	public static OlympaPlayer getFromDatabase(String name) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public static OlympaPlayer getFromDatabase(UUID uuid) throws SQLException {
		throw new UnsupportedOperationException();
	}

	public static String getPlayerProviderTableName() {
		throw new UnsupportedOperationException();
	}

	public static void setPlayerProvider(Class<? extends OlympaPlayerObject> playerClass, OlympaPlayerProvider provider, String pluginName, Map<String, String> columns) {
		AccountProvider.provider = provider;
	}

	private UUID uuid;

	public AccountProvider(UUID uuid) {
		this.uuid = uuid;
	}

	@Override
	public OlympaPlayer createOlympaPlayer(String name, String ip) {
		return provider.create(uuid, name, ip);
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
