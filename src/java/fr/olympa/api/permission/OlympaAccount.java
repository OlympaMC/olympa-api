package fr.olympa.api.permission;

import java.sql.SQLException;
import java.util.function.Consumer;

import fr.olympa.api.objects.OlympaPlayer;

public interface OlympaAccount {

	OlympaPlayer createOlympaPlayer(String name, String ip);

	OlympaPlayer get() throws SQLException;

	void saveToDb(OlympaPlayer olympaPlayer);

	void saveToRedis(OlympaPlayer olympaPlayer);

	void sendModifications(OlympaPlayer olympaPlayer);

	void sendModifications(OlympaPlayer olympaPlayer, Consumer<? super Boolean> done);

	void sendModificationsReceive();
}