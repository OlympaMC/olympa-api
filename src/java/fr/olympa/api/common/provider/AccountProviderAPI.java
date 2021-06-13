package fr.olympa.api.common.provider;

import java.sql.SQLException;
import java.util.UUID;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaAccount;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.PlayerSQL;
import fr.olympa.api.common.redis.RedisConnection;
import fr.olympa.api.common.sql.SQLTable;
import redis.clients.jedis.Jedis;

public class AccountProviderAPI implements OlympaAccount {

	protected static AccountProviderGetterInterface getter;
	protected static RedisConnection redisAccesss;
	protected static PlayerSQL playerSQL;
	protected static SQLTable<OlympaPlayerObject> olympaPlayerTable;

	public static AccountProviderGetterInterface getter() {
		return getter;
	}

	public static void init(PlayerSQL sqlClass, AccountProviderGetterInterface getterInterface) throws SQLException {
		olympaPlayerTable = new SQLTable<>(sqlClass.getTableCleanName(), OlympaPlayer.COLUMNS).createOrAlter();
		playerSQL = sqlClass;
		getter = getterInterface;
	}

	public static void setRedisConnection(RedisConnection redisConnection) {
		redisAccesss = redisConnection;
	}

	UUID uuid;

	public AccountProviderAPI(UUID uuid) {
		this.uuid = uuid;
	}

	protected String getKey() {
		return REDIS_KEY + uuid.toString();
	}

	@Override
	public OlympaPlayer createOlympaPlayer(String name, String ip) {
		OlympaPlayer newOlympaPlayer = getter.getOlympaPlayerProvider().create(uuid, name, ip);
		newOlympaPlayer.setGroup(OlympaGroup.PLAYER);
		return newOlympaPlayer;
	}

	public OlympaPlayer fromDb() throws SQLException {
		return playerSQL.getPlayer(uuid);
	}

	@Override
	public OlympaPlayer get() throws SQLException {
		OlympaPlayer olympaPlayer = getFromCache();
		if (olympaPlayer == null) {
			olympaPlayer = getFromRedis();
			if (olympaPlayer == null)
				return fromDb();
		}
		return olympaPlayer;
	}

	@Override
	public OlympaPlayer getFromCache() {
		return cache.get(uuid);
	}

	@Override
	public OlympaPlayer getFromRedis() {
		String json = null;
		try (Jedis jedis = redisAccesss.connect()) {
			json = jedis.get(getKey());
		}
		redisAccesss.disconnect();

		if (json == null || json.isEmpty())
			return null;
		return LinkSpigotBungee.getInstance().getGson().fromJson(json, getter.getPlayerClass());
	}

	@Override
	public void removeFromCache() {
		cache.remove(uuid);
	}

	@Override
	public void saveToRedis(OlympaPlayer olympaPlayer) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
			try (Jedis jedis = redisAccesss.connect()) {
				jedis.set(getKey(), LinkSpigotBungee.getInstance().getGson().toJson(olympaPlayer));
			}
			redisAccesss.disconnect();
		});
	}
}
