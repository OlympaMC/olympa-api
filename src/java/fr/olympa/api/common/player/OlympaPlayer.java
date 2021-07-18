package fr.olympa.api.common.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.provider.OlympaPlayerCore;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.sql.SQLColumn;

public interface OlympaPlayer {

	SQLColumn<OlympaPlayerCore> COLUMN_ID = new SQLColumn<OlympaPlayerCore>("id", "INT(20) unsigned NOT NULL AUTO_INCREMENT", Types.INTEGER).setPrimaryKey(OlympaPlayerCore::getId);
	SQLColumn<OlympaPlayerCore> COLUMN_PSEUDO = new SQLColumn<OlympaPlayerCore>("pseudo", "VARCHAR(255) NOT NULL", Types.VARCHAR).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_UUID_SERVER = new SQLColumn<>("uuid_server", "VARCHAR(36) NOT NULL", Types.VARCHAR);
	SQLColumn<OlympaPlayerCore> COLUMN_UUID_PREMIUM = new SQLColumn<OlympaPlayerCore>("uuid_premium", "VARCHAR(36) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_GROUPS = new SQLColumn<OlympaPlayerCore>("groups", "VARCHAR(45) DEFAULT '20'", Types.VARCHAR).setUpdatable().setNotDefault();
	SQLColumn<OlympaPlayerCore> COLUMN_EMAIL = new SQLColumn<OlympaPlayerCore>("email", "VARCHAR(255) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_PASSWORD = new SQLColumn<OlympaPlayerCore>("password", "VARCHAR(512) DEFAULT NULL", Types.VARCHAR).setUpdatable().setNotDefault();
	SQLColumn<OlympaPlayerCore> COLUMN_MONEY = new SQLColumn<>("money", "VARCHAR(20) DEFAULT '0'", Types.VARCHAR); // unused?
	SQLColumn<OlympaPlayerCore> COLUMN_IP = new SQLColumn<OlympaPlayerCore>("ip", "VARCHAR(39) NOT NULL", Types.VARCHAR).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_CREATED = new SQLColumn<OlympaPlayerCore>("created", "DATE NOT NULL", Types.DATE).setUpdatable().setNotDefault();
	SQLColumn<OlympaPlayerCore> COLUMN_LAST_CONNECTION = new SQLColumn<OlympaPlayerCore>("last_connection", "TIMESTAMP NULL DEFAULT current_timestamp()", Types.TIMESTAMP).setUpdatable().setNotDefault();
	SQLColumn<OlympaPlayerCore> COLUMN_TS3_ID = new SQLColumn<OlympaPlayerCore>("ts3_id", "INT(11) DEFAULT NULL", Types.INTEGER).setUpdatable();
	//	private static final SQLColumn<OlympaPlayerObject> COLUMN_DISCORD_ID = new SQLColumn<OlympaPlayerObject>("discord_olympa_id", "INT(11) DEFAULT NULL", Types.INTEGER).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_NAME_HISTORY = new SQLColumn<OlympaPlayerCore>("name_history", "TEXT(65535) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_IP_HISTORY = new SQLColumn<OlympaPlayerCore>("ip_history", "TEXT(65535) DEFAULT NULL", Types.VARCHAR).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_GENDER = new SQLColumn<OlympaPlayerCore>("gender", "TINYINT(1) DEFAULT NULL", Types.TINYINT).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_VANISH = new SQLColumn<OlympaPlayerCore>("vanish", "TINYINT(1) DEFAULT 0", Types.TINYINT).setUpdatable();
	SQLColumn<OlympaPlayerCore> COLUMN_CUSTOM_PERMISSIONS = new SQLColumn<OlympaPlayerCore>("custom_permissions", "TEXT(65535) DEFAULT NULL", Types.VARCHAR).setUpdatable();

	List<SQLColumn<OlympaPlayerCore>> COLUMNS = Arrays.asList(COLUMN_ID, COLUMN_PSEUDO, COLUMN_UUID_SERVER, COLUMN_UUID_PREMIUM, COLUMN_GROUPS, COLUMN_EMAIL, COLUMN_PASSWORD, COLUMN_MONEY, COLUMN_IP, COLUMN_CREATED,
			COLUMN_LAST_CONNECTION, COLUMN_TS3_ID/*, COLUMN_DISCORD_ID*/, COLUMN_NAME_HISTORY, COLUMN_IP_HISTORY, COLUMN_GENDER, COLUMN_VANISH, COLUMN_CUSTOM_PERMISSIONS);

	void addGroup(OlympaGroup group);

	void addGroup(OlympaGroup group, long time);

	void addNewIp(String ip);

	void addNewName(String name);

	OlympaPlayer clone();

	String getEmail();

	long getFirstConnection();

	Gender getGender();

	OlympaGroup getGroup();

	String getGroupName();

	String getGroupNameColored();

	String getGroupPrefix();

	Map<OlympaGroup, Long> getGroups();

	String getGroupsToHumainString();

	String getGroupsToString();

	Map<Long, String> getHistName();

	Map<Long, String> getHistIp();

	Map<String, OlympaServer> getCustomPermissions();

	long getId();

	OlympaPlayerInformations getInformation();

	String getIp();

	long getLastConnection();

	String getName();

	default String getNameWithPrefix() {
		return getGroupPrefix() + getName();
	}

	String getPassword();

	Object getPlayer();

	UUID getPremiumUniqueId();

	int getTeamspeakId();

	String getTuneChar();

	UUID getUniqueId();

	boolean isConnected();

	boolean isGenderFemale();

	boolean isPremium();

	boolean isSamePassword(String password);

	boolean isVanish();

	void loadDatas(ResultSet resultSet) throws SQLException;

	void loaded();

	void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson/*, int discordOlympaId*/, int teamspeakId, boolean vanish);

	void unloaded();

	void removeGroup(OlympaGroup group);

	void setConnected(boolean connected);

	void setEmail(String email);

	void setGender(Gender gender);

	void setGroup(OlympaGroup group);

	void setGroup(OlympaGroup group, long time);

	void setId(long id);

	void setIp(String ip);

	void setLastConnection(long lastConnection);

	void setName(String name);

	void setPassword(String password);

	void setPremiumUniqueId(UUID premium_uuid);

	void setTeamspeakId(int teamspeakId);

	void setVanish(boolean vanish);

	boolean hasCustomPermission(String permission, OlympaServer serv);

	void removeCustomPermission(OlympaPermission perm, OlympaServer serv);

	void addCustomPermission(OlympaPermission perm, OlympaServer serv);

}