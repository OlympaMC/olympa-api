package fr.olympa.api.objects;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public interface OlympaPlayer {

	void addGroup(OlympaGroup group);

	void addGroup(OlympaGroup group, long time);

	void addNewIp(String ip);

	void addNewName(String name);

	OlympaPlayer clone();

	long getDiscordId();

	String getEmail();

	long getFirstConnection();

	Gender getGender();

	OlympaGroup getGroup();

	String getGroupName();

	String getGroupNameColored();

	String getGroupPrefix();

	TreeMap<OlympaGroup, Long> getGroups();

	String getGroupsToHumainString();

	String getGroupsToString();

	TreeMap<Long, String> getHistHame();

	TreeMap<Long, String> getHistIp();

	long getId();

	OlympaPlayerInformations getInformation();

	String getIp();

	long getLastConnection();

	String getName();

	String getPassword();

	Player getPlayer();

	UUID getPremiumUniqueId();

	OlympaMoney getStoreMoney();

	long getTeamspeakId();

	String getTuneChar();

	UUID getUniqueId();

	boolean hasPermission(OlympaPermission permission);

	boolean isAfk();

	boolean isConnected();

	boolean isGenderFemale();

	boolean isPremium();

	boolean isSamePassword(String password);

	boolean isVanish();

	boolean isVerifMode();

	void loadDatas(ResultSet resultSet) throws SQLException;

	void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson);

	void removeGroup(OlympaGroup newGroup);

	void saveDatas(PreparedStatement statement) throws SQLException;

	void setAfk(boolean afk);

	void setConnected(boolean connected);

	void setDiscordId(long discordId);

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

	void setTeamspeakId(long teamspeakId);

	void setVanish(boolean vanish);

	void setVerifMode(boolean verifMode);

}