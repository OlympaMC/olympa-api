package fr.olympa.api.objects;

import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.permission.OlympaPermission;

public interface OlympaPlayer {

	void addGroup(OlympaGroup group);

	void addGroup(OlympaGroup group, long time);

	void addMoney(double money);

	OlympaPlayer clone();

	String getEmail();

	long getFirstConnection();

	OlympaGroup getGroup();

	TreeMap<OlympaGroup, Long> getGroups();

	String getGroupsToHumainString();

	String getGroupsToString();

	int getId();

	String getIp();

	long getLastConnection();

	double getMoney();

	String getName();

	String getPassword();

	Player getPlayer();

	UUID getPremiumUniqueId();

	UUID getUniqueId();

	boolean hasPermission(OlympaPermission permission);

	boolean isAfk();

	boolean isSamePassword(String password);

	boolean isVanish();

	boolean isVerifMode();

	void removeMoney(double money);

	void setAfk(boolean afk);

	void setGroup(OlympaGroup group);

	void setGroup(OlympaGroup group, long time);

	void setGroupsFromString(String groupsString);

	void setId(int id);

	void setIp(String ip);

	void setLastConnection(long lastConnection);

	void setMoney(double money);

	void setName(String name);

	void setPassword(String password);

	void setPremiumUniqueId(UUID premium_uuid);

	void setUniqueId(UUID uuid);

	void setVanish(boolean vanish);

	void setVerifMode(boolean verifMode);

}