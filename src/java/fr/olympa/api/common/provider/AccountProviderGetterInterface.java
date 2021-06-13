package fr.olympa.api.common.provider;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.player.OlympaPlayerProvider;
import fr.olympa.api.common.player.PlayerSQL;
import fr.olympa.api.common.sql.SQLColumn;
import fr.olympa.api.common.sql.SQLTable;

public interface AccountProviderGetterInterface {

	<T extends OlympaPlayer> T get(String name) throws SQLException;

	<T extends OlympaPlayer> T get(long id) throws SQLException;

	<T extends OlympaPlayer> T get(UUID uuid);

	Collection<OlympaPlayer> getAll();

	Collection<OlympaPlayerInformations> getAllPlayersInformations();

	List<OlympaPlayerInformations> getAllConnectedPlayersInformations();

	OlympaPlayer getFromDatabase(String name) throws SQLException;

	OlympaPlayer getFromDatabase(UUID uuid) throws SQLException;

	OlympaPlayer getFromDatabase(long id) throws SQLException;

	OlympaPlayer getFromRedis(String name);

	OlympaPlayer getFromRedis(long id);

	OlympaPlayerInformations getPlayerInformations(long id);

	OlympaPlayerInformations getPlayerInformations(UUID uuid);

	OlympaPlayerInformations getPlayerInformations(String name);

	OlympaPlayerInformations getPlayerInformations(OlympaPlayer player);

	SQLTable<? extends OlympaPlayer> getPluginPlayerTable();

	<T extends OlympaPlayer> void setPlayerProvider(Class<T> playerClass, OlympaPlayerProvider provider, String pluginName, List<SQLColumn<T>> columns);

	boolean loadPlayerDatas(OlympaPlayer player) throws SQLException;

	Map<Long, OlympaPlayerInformations> getCachedInformations();

	PlayerSQL getSQL();

	Class<? extends OlympaPlayer> getPlayerClass();
	
	OlympaPlayerProvider getOlympaPlayerProvider();
	
}