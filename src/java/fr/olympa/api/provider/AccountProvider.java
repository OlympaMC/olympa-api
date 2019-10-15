package fr.olympa.api.provider;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaAccount;

public class AccountProvider implements OlympaAccount {
	;
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

	public AccountProvider(UUID playerUniqueId) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void accountExpire(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean createNew(OlympaPlayer olympaPlayer, String name, String ip) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public OlympaPlayer fromDb() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OlympaPlayer get() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OlympaPlayer getFromCache() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void removeFromCache() {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToCache(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToDb(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void saveToRedis(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendModifications(OlympaPlayer olympaPlayer) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendModifications(OlympaPlayer olympaPlayer, Consumer<? super Boolean> done) {
		// TODO Auto-generated method stub

	}

	@Override
	public void sendModificationsReceive() {
		// TODO Auto-generated method stub

	}

}
