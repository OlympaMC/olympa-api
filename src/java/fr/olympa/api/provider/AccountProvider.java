package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.player.OlympaAccount;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.player.OlympaPlayerProvider;
import fr.olympa.api.sql.SQLColumn;

public class AccountProvider implements OlympaAccount {

	public static Map<UUID, OlympaPlayer> cache = new HashMap<>();
	public static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();
	private static OlympaPlayerProvider provider = OlympaPlayerObject::new;

	public static <T extends OlympaPlayer> T get(UUID uuid) {
		return (T) cache.get(uuid);
	}

	public static <T extends OlympaPlayer> T get(String name) throws SQLException {
		OlympaPlayer olympaPlayer = null/*= AccountProvider.getFromCache(name)*/;
		//				if (olympaPlayer == null)
		//					olympaPlayer = AccountProvider.getFromRedis(name);
		if (olympaPlayer == null)
			olympaPlayer = AccountProvider.getFromDatabase(name);
		return (T) olympaPlayer;
	}

	public static <T extends OlympaPlayer> T get(long id) throws SQLException {
		OlympaPlayer olympaPlayer = null/*= AccountProvider.getFromCache(name)*/;
		//				if (olympaPlayer == null)
		//					olympaPlayer = AccountProvider.getFromRedis(name);
		if (olympaPlayer == null)
			olympaPlayer = AccountProvider.getFromDatabase(id);
		return (T) olympaPlayer;
	}

	public static OlympaPlayerInformations getPlayerInformations(long id) {
		return cachedInformations.get(id);
	}

	public static OlympaPlayer getFromDatabase(long id) throws SQLException {
		throw new UnsupportedOperationException();
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

	public static <T extends OlympaPlayer> void setPlayerProvider(Class<T> playerClass, OlympaPlayerProvider provider, String pluginName, List<SQLColumn<T>> columns) {
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

	/*@Override
	public void saveToDb(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub
	
	}*/

	@Override
	public void saveToRedis(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}
}
