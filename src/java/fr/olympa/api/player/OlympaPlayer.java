package fr.olympa.api.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.server.OlympaServer;

public interface OlympaPlayer {

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

	Map<Long, String> getHistHame();

	Map<Long, String> getHistIp();

	Map<String, OlympaServer> getCustomPermissions();

	Map<Long, String> getHistName();

	long getId();

	OlympaPlayerInformations getInformation();

	String getIp();

	long getLastConnection();

	String getName();
	
	default String getNameWithPrefix() {
		return getGroupPrefix() + getName();
	}

	String getPassword();

	Player getPlayer();

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

	//	int getDiscordOlympaId();
	//
	//	void setDiscordOlympaId(int discordOlympaId);

}