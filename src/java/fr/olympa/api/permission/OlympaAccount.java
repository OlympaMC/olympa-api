package fr.olympa.api.permission;

import java.sql.SQLException;

import fr.olympa.api.objects.OlympaPlayer;

public interface OlympaAccount {

	OlympaPlayer createOlympaPlayer(String name, String ip);

	OlympaPlayer get() throws SQLException;

	void saveToDb(OlympaPlayer olympaPlayer) throws SQLException;

	void saveToRedis(OlympaPlayer olympaPlayer);
}