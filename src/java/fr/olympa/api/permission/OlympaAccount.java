package fr.olympa.api.permission;

import java.sql.SQLException;
import java.util.function.Consumer;

import fr.olympa.api.objects.OlympaPlayer;

public interface OlympaAccount {

	void accountExpire(OlympaPlayer olympaPlayer);

	boolean createNew(OlympaPlayer olympaPlayer, String name, String ip);

	OlympaPlayer fromDb() throws SQLException;

	OlympaPlayer get() throws SQLException;

	OlympaPlayer getFromCache();

	void removeFromCache();

	void saveToCache(OlympaPlayer olympaPlayer);

	void saveToDb(OlympaPlayer olympaPlayer);

	void saveToRedis(OlympaPlayer olympaPlayer);

	void sendModifications(OlympaPlayer olympaPlayer);

	void sendModifications(OlympaPlayer olympaPlayer, Consumer<? super Boolean> done);

	void sendModificationsReceive();

}