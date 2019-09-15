package fr.tristiisch.olympa.api.permission;

import java.sql.SQLException;
import java.util.UUID;
import java.util.function.Consumer;

import fr.tristiisch.olympa.api.objects.OlympaPlayer;

public class OlympaAccountObject implements OlympaAccount {
	UUID uuid;

	public OlympaAccountObject(final UUID uuid) {
		this.uuid = uuid;
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
