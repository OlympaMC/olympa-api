package fr.olympa.api.objects;

import java.util.Arrays;
import java.util.Comparator;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.utils.Utils;

public class OlympaPlayerObject implements OlympaPlayer, Cloneable {

	int id;
	UUID uuid;
	UUID premium_uuid;
	String name;
	TreeMap<OlympaGroup, Long> groups = new TreeMap<>(Comparator.comparing(OlympaGroup::getPower).reversed());
	String ip;
	long firstConnection;
	long lastConnection;

	public OlympaPlayerObject(int id, UUID uuid, String name, String groupsString, String ip, long firstConnection, long lastConnection) {
		this.id = id;
		this.uuid = uuid;
		this.name = name;
		this.setGroupsFromString(groupsString);
		this.ip = ip;
		this.firstConnection = firstConnection;
		this.lastConnection = lastConnection;
	}

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
		this.groups.put(OlympaGroup.PLAYER, 0l);
		this.firstConnection = Utils.getCurrentTimeinSeconds();
		this.lastConnection = Utils.getCurrentTimeinSeconds();
	}

	@Override
	public void addGroup(OlympaGroup group) {
		this.addGroup(group, 0l);
	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
		this.groups.put(group, time);
		if (this.groups.size() > 1 && this.groups.containsKey(OlympaGroup.PLAYER)) {
			this.removeGroup(OlympaGroup.PLAYER);
		}
	}

	@Override
	public OlympaPlayer clone() {
		try {
			return (OlympaPlayer) super.clone();
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public long getFirstConnection() {
		return this.firstConnection;
	}

	@Override
	public OlympaGroup getGroup() {
		return this.groups.firstKey();
	}

	@Override
	public TreeMap<OlympaGroup, Long> getGroups() {
		return this.groups;
	}

	@Override
	public String getGroupsToHumainString() {
		return this.groups.entrySet().stream().map(entry -> {
			String time = new String();
			if (entry.getValue() != 0) {
				time = " (" + Utils.timestampToDateAndHour(entry.getValue()) + ")";
			}
			return entry.getKey().getName() + time;
		}).collect(Collectors.joining(", "));
	}

	@Override
	public String getGroupsToString() {
		return this.groups.entrySet().stream().map(entry -> entry.getKey().getId() + (entry.getValue() != 0 ? ":" + entry.getValue() : "")).collect(Collectors.joining(";"));
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getIp() {
		return this.ip;
	}

	@Override
	public long getLastConnection() {
		return this.lastConnection;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public Player getPlayer() {
		return Bukkit.getPlayer(this.uuid);
	}

	@Override
	public UUID getPremiumUniqueId() {
		return this.premium_uuid;
	}

	@Override
	public UUID getUniqueId() {
		return this.uuid;
	}

	@Override
	public boolean hasPermission(OlympaPermission permission) {
		return this.groups.keySet().stream().filter(group -> group.getPower() >= permission.getGroup().getPower()).findFirst().isPresent();
	}

	@Override
	public boolean hasPower(OlympaGroup group2) {
		return this.groups.keySet().stream().filter(group -> group.getPower() >= group2.getPower()).findFirst().isPresent();
	}

	@Override
	public boolean hasPower(OlympaGroup[] groups2) {
		return Arrays.stream(groups2).filter(group -> this.hasPower(group)).findFirst().isPresent();
	}

	@Override
	public boolean isSamePower(OlympaGroup group2) {
		return this.groups.keySet().stream().filter(group -> group.getPower() == group2.getPower()).findFirst().isPresent();
	}

	private void removeGroup(OlympaGroup group) {
		this.groups.remove(group);
	}

	@Override
	public void setGroup(OlympaGroup group) {
		this.setGroup(group, 0l);
	}

	@Override
	public void setGroup(OlympaGroup group, long time) {
		this.groups.clear();
		this.addGroup(group, time);
	}

	@Override
	public void setGroupsFromString(final String groupsString) {
		for (final String groupInfos : groupsString.split(";")) {
			final String[] groupInfo = groupInfos.split(":");
			final OlympaGroup olympaGroup = OlympaGroup.getById(Integer.parseInt(groupInfo[0]));
			final Long until;
			if (groupInfo.length > 1) {
				until = Long.parseLong(groupInfo[1]);
			} else {
				until = 0l;
			}
			this.groups.put(olympaGroup, until);
		}
	}

	@Override
	public void setId(int id) {
		this.id = id;
	}

	@Override
	public void setIp(String ip) {
		this.ip = ip;
	}

	@Override
	public void setLastConnection(long lastConnection) {
		this.lastConnection = lastConnection;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPremiumUniqueId(UUID premium_uuid) {
		this.premium_uuid = premium_uuid;
	}

	@Override
	public void setUniqueId(UUID uuid) {
		this.uuid = uuid;
	}
}
