package fr.olympa.api.common.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.player.OlympaAccount;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.player.OlympaPlayerProvider;
import fr.olympa.api.common.player.PlayerSQL;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;

@SuppressWarnings({ "unchecked", "unused" })
public class AccountProvider implements OlympaAccount {

	public static Map<UUID, OlympaPlayer> cache = new HashMap<>();
	public static Map<Long, OlympaPlayerInformations> cachedInformations = new HashMap<>();

	private static OlympaPlayerProvider provider = OlympaPlayerObject::new;
	public static OlympaPlayerProvider pluginPlayerProvider = OlympaPlayerObject::new;
	private static SQLTable<? extends OlympaPlayer> pluginPlayerTable = null;

	private static SQLTable<OlympaPlayerObject> olympaPlayerTable;
	private static PlayerSQL playerSQL;

	public static void init(PlayerSQL sqlClass) throws SQLException {
		olympaPlayerTable = null/*new SQLTable<>(qlClass.getTableCleanName(), OlympaPlayerObject.COLUMNS).createOrAlter()*/;
		playerSQL = sqlClass;
	}

	public static PlayerSQL getSQL() {
		return null;
	}

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

	public static Collection<OlympaPlayer> getAll() {
		return cache.values();
	}

	public static List<OlympaPlayerInformations> getAllConnectedPlayersInformations() {
		List<OlympaPlayerInformations> list = new ArrayList<>();
		cache.forEach((uuid, olympaPlayer) -> {
			if (olympaPlayer.isConnected())
				list.add(getPlayerInformations(olympaPlayer.getId()));
		});
		return list;
	}

	public static Collection<OlympaPlayerInformations> getAllPlayersInformations() {
		return cachedInformations.values();
	}

	public static synchronized OlympaPlayerInformations getPlayerInformations(long id) {
		return cachedInformations.get(id);
	}

	public static synchronized OlympaPlayerInformations getPlayerInformations(String name) {
		return cachedInformations.values().stream().filter(opi -> opi.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static synchronized OlympaPlayerInformations getPlayerInformations(UUID uuid) {
		return cachedInformations.values().stream().filter(opi -> opi.getUUID().equals(uuid)).findFirst().orElse(null);
	}

	public synchronized static OlympaPlayerInformations getPlayerInformations(OlympaPlayer player) {
		return cachedInformations.get(player.getId());
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

	public static OlympaPlayer getFromRedis(String name) {
		OlympaPlayer olympaPlayer = null;
		//		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
		//			olympaPlayer = jedis.keys("player:*").stream().filter(v -> v.contains(name)).map(v -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(v, playerClass))
		//					.filter(p -> p.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
		//		}
		//		RedisAccess.INSTANCE.disconnect();
		return olympaPlayer;
	}

	public static OlympaPlayer getFromRedis(long id) {
		OlympaPlayer olympaPlayer = null;
		//		try (Jedis jedis = RedisAccess.INSTANCE.connect()) {
		//			olympaPlayer = jedis.keys("player:*").stream().filter(v -> v.contains(String.valueOf(id))).map(value -> GsonCustomizedObjectTypeAdapter.GSON.fromJson(value, playerClass))
		//					.filter(p -> p.getId() == id).findFirst().orElse(null);
		//		}
		//		RedisAccess.INSTANCE.disconnect();
		return olympaPlayer;
	}

	public static SQLTable<? extends OlympaPlayer> getPluginPlayerTable() {
		return pluginPlayerTable;
	}

	public static <T extends OlympaPlayer> void setPlayerProvider(Class<T> playerClass, OlympaPlayerProvider provider, String pluginName, List<SQLColumn<T>> columns) {
		AccountProvider.provider = provider;
	}

	public static boolean loadPlayerDatas(OlympaPlayer player) throws SQLException {
		if (pluginPlayerTable == null)
			return false;
		ResultSet resultSet = pluginPlayerTable.get(player.getId());
		if (resultSet.next()) {
			player.loadDatas(resultSet);
			player.loaded();
			return false;
		}
		pluginPlayerTable.insert(player.getId());
		LinkSpigotBungee.Provider.link.sendMessage("Données créées pour le joueur §6%s", player.getName());
		player.loaded();
		return true;
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
	public void saveToRedis(OlympaPlayer olympaPlayer) {}
}
