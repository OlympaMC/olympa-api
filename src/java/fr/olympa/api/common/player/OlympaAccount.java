package fr.olympa.api.common.player;

import java.sql.SQLException;

public interface OlympaAccount {

	OlympaPlayer createOlympaPlayer(String name, String ip);

	OlympaPlayer get() throws SQLException;

	//void saveToDb(OlympaPlayer olympaPlayer) throws SQLException;

	void saveToRedis(OlympaPlayer olympaPlayer);

	static PlayerSQL getSQL() {
		return null;
	}
}