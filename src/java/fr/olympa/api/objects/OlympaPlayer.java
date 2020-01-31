package fr.olympa.api.objects;

import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public interface OlympaPlayer {

	void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson);

	void addGroup(OlympaGroup group);

	void addGroup(OlympaGroup group, long time);

	void addMoney(double money);

	void addNewIp(String ip);

	void addNewName(String name);

	OlympaPlayer clone();

	String getEmail();

	long getFirstConnection();

	Gender getGender();

	OlympaGroup getGroup();

	TreeMap<OlympaGroup, Long> getGroups();

	String getGroupsToHumainString();

	String getGroupsToString();

	TreeMap<Long, String> getHistHame();

	TreeMap<Long, String> getHistIp();

	long getId();

	String getIp();

	long getLastConnection();

	double getMoney();

	String getName();

	String getPassword();

	Player getPlayer();

	UUID getPremiumUniqueId();

	UUID getUniqueId();

	void giveMoney(double money);

	boolean hasMoney(double money);

	boolean hasPermission(OlympaPermission permission);

	boolean isAfk();

	boolean isPremium();

	boolean isSamePassword(String password);

	boolean isVanish();

	boolean isVerifMode();

	void setId(long id);

	void setAfk(boolean afk);

	void setGender(Gender gender);

	void setGroup(OlympaGroup group);

	void setGroup(OlympaGroup group, long time);

	void setIp(String ip);

	void setLastConnection(long lastConnection);

	void setMoney(double money);

	void setName(String name);

	void setPassword(String password);

	void setPremiumUniqueId(UUID premium_uuid);

	void setVanish(boolean vanish);

	void setVerifMode(boolean verifMode);

	boolean withdrawMoney(double money);

}