package fr.olympa.api.player;

import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import fr.olympa.api.groups.OlympaGroup;

public interface PlayerSQL {

	/**
	 * Récupère le nom exacte d'un joueur dans la base de données à l'aide de son
	 * UUID
	 * @throws SQLException
	 */
	String getNameFromUuid(UUID uuid) throws SQLException;

	/**
	 * Permet de récupérer les informations d'un joueur dans la base de données grâce à
	 * son id
	 * @throws SQLException
	 */
	OlympaPlayerInformations getPlayerInformations(long id) throws SQLException;

	/**
	 * Permet de récupérer les informations d'un joueur dans la base de données grâce à
	 * son nom
	 * @throws SQLException
	 */
	OlympaPlayerInformations getPlayerInformations(String name) throws SQLException;

	/**
	 * Permet de récupérer les informations d'un joueur dans la base de données grâce à
	 * son id
	 * @throws SQLException
	 */
	OlympaPlayerInformations getPlayerInformations(UUID uuid) throws SQLException;

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son id
	 * @throws SQLException
	 */
	OlympaPlayer getPlayer(long id) throws SQLException;

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son pseudo
	 * @throws SQLException
	 */
	OlympaPlayer getPlayer(String playerName) throws SQLException;

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid
	 *
	 * @throws SQLException
	 */
	OlympaPlayer getPlayer(UUID playerUUID) throws SQLException;

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son uuid premium
	 * @throws SQLException
	 */
	OlympaPlayer getPlayerByPremiumUuid(UUID premiumUUID) throws SQLException;

	/**
	 * Permet de récupérer les donnés d'un joueur dans la base de données grâce à
	 * son ts3databaseid
	 * @throws SQLException
	 */
	OlympaPlayer getPlayerByTs3Id(int ts3databaseid) throws SQLException;

	List<OlympaPlayer> getPlayersByNameHistory(String nameHistory) throws SQLException;

	Set<OlympaPlayer> getPlayersByRegex(String regex) throws SQLException;

	Set<String> getPlayersBySimilarChars(String name) throws SQLException;

	Set<OlympaPlayer> getPlayersBySimilarName(String name) throws SQLException;

	Set<OlympaPlayer> getPlayersByGroupsIds(OlympaGroup... groups) throws SQLException;

	Set<OlympaPlayer> getPlayersByGroupsIds(List<OlympaGroup> groups) throws SQLException;

	Set<String> getNamesBySimilarChars(String name) throws SQLException;

	Set<String> getNamesBySimilarName(String name);

	/**
	 * Récupère l'uuid d'un joueur dans la base de données à l'aide de son pseudo
	 * @throws SQLException
	 */
	UUID getPlayerUniqueId(String playerName) throws SQLException;

	boolean playerExist(UUID playerUUID) throws SQLException;

}