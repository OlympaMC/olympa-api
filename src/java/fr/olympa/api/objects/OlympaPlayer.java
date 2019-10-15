package fr.olympa.api.objects;

import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.permission.OlympaPermission;

public interface OlympaPlayer {

	void addGroup(OlympaGroup group);

	void addGroup(OlympaGroup group, long time);

	OlympaPlayer clone();

	long getFirstConnection();

	OlympaGroup getGroup();

	TreeMap<OlympaGroup, Long> getGroups();

	String getGroupsToHumainString();

	String getGroupsToString();

	int getId();

	String getIp();

	long getLastConnection();

	String getName();

	Player getPlayer();

	UUID getPremiumUniqueId();

	UUID getUniqueId();

	boolean hasPermission(OlympaPermission permission);

	boolean hasPower(OlympaGroup group2);

	boolean hasPower(OlympaGroup[] groups2);

	boolean isSamePower(OlympaGroup group2);

	void setGroup(OlympaGroup group);

	void setGroup(OlympaGroup group, long time);

	void setGroupsFromString(String groupsString);

	void setId(int id);

	void setIp(String ip);

	void setLastConnection(long lastConnection);

	void setName(String name);

	void setPremiumUniqueId(UUID premium_uuid);

	void setUniqueId(UUID uuid);

}