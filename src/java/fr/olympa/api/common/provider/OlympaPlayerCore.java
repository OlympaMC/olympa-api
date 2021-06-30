package fr.olympa.api.common.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import com.google.gson.annotations.Expose;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.utils.Utils;

public abstract class OlympaPlayerCore implements OlympaPlayer, Cloneable {

	@Expose
	public long id = -1;
	@Expose
	UUID uuid;
	@Expose
	String name;
	@Expose
	String ip;
	@Expose
	public Map<OlympaGroup, Long> groups = new TreeMap<>(Comparator.comparing(OlympaGroup::getPower).reversed());
	@Expose
	public Map<Long, String> histName = new TreeMap<>(Comparator.comparing(Long::longValue).reversed());
	@Expose
	public Gender gender = Gender.UNSPECIFIED;
	@Expose
	public boolean vanish;
	@Expose
	boolean connected;
	@Expose
	public int teamspeakId;
	@Expose
	public long firstConnection;
	@Expose
	public long lastConnection;

	private OlympaPlayerInformations cachedInformations = null;
	private Object cachedPlayer = null;

	protected OlympaPlayerCore(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
	}

	@Override
	public void addGroup(OlympaGroup group) {
		this.addGroup(group, 0l);
	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
		groups.put(group, time);
	}

	@Override
	public OlympaPlayer clone() {
		try {
			return (OlympaPlayer) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Gender getGender() {
		return gender;
	}

	@Override
	public void setGender(Gender gender) {
		this.gender = gender;
	}

	@Override
	public OlympaGroup getGroup() {
		OlympaGroup olympaGroup = null;
		for (Iterator<OlympaGroup> iterator = groups.keySet().iterator(); iterator.hasNext();) {
			olympaGroup = iterator.next();
			if (!olympaGroup.isVisible())
				continue;
			if (OlympaServer.ALL.equals(olympaGroup.getServer()))
				break;
			if (Objects.equals(olympaGroup.getServer(), LinkSpigotBungee.Provider.link.getOlympaServer()))
				break;
		}
		return olympaGroup;
	}

	@Override
	public String getGroupName() {
		return getGroup().getName(gender);
	}

	@Override
	public String getGroupNameColored() {
		return getGroup().getColor() + getGroupName();
	}

	@Override
	public String getGroupPrefix() {
		return getGroup().getPrefix(gender);
	}

	@Override
	public Map<OlympaGroup, Long> getGroups() {
		return groups;
	}

	@Override
	public String getGroupsToHumainString() {
		return groups.entrySet().stream().map(entry -> {
			String time = null;
			if (entry.getValue() != 0)
				time = " (" + Utils.tsToShortDur(entry.getValue()) + ")";
			return entry.getKey().getName(gender) + (time != null ? time : "");
		}).collect(Collectors.joining(", "));
	}

	@Override
	public String getGroupsToString() {
		return groups.entrySet().stream().map(entry -> entry.getKey().getId() + (entry.getValue() != 0 ? ":" + entry.getValue() : "")).collect(Collectors.joining(";"));
	}

	@Override
	public Map<Long, String> getHistName() {
		return histName;
	}

	@Override
	public long getId() {
		return id;
	}

	public OlympaPlayerInformations getCachedInformations() {
		return cachedInformations;
	}

	@Override
	public OlympaPlayerInformations getInformation() {
		if (cachedInformations == null)
			cachedInformations = AccountProviderAPI.getter().getPlayerInformations(this);
		return cachedInformations;
	}

	@Override
	public String getIp() {
		return ip;
	}

	@Override
	public long getLastConnection() {
		return lastConnection;
	}

	@Override
	public String getName() {
		return name;
	}

	public Object getCachedPlayer() {
		return cachedPlayer;
	}

	@Override
	public Object getPlayer() {
		if (cachedPlayer == null)
			cachedPlayer = LinkSpigotBungee.getInstance().getPlayer(uuid);
		return cachedPlayer;
	}

	@Override
	public UUID getPremiumUniqueId() {
		return uuid;
	}

	@Override
	public String getTuneChar() {
		return gender.getTurne();
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public boolean isConnected() {
		return connected;
	}

	@Override
	public boolean isGenderFemale() {
		return gender == Gender.FEMALE;
	}

	@Override
	public boolean isVanish() {
		return vanish;
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {}

	@Override
	public void loaded() {}

	@Override
	public void unloaded() {}

	@Override
	public void removeGroup(OlympaGroup group) {
		groups.remove(group);
	}

	@Override
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	@Override
	public void setGroup(OlympaGroup group) {
		this.setGroup(group, 0l);
	}

	@Override
	public void setGroup(OlympaGroup group, long time) {
		groups.clear();
		this.addGroup(group, time);
	}

	@Override
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	@Override
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public void setLastConnection(long lastConnection) {
		this.lastConnection = lastConnection;
	}

	@Override
	public void setVanish(boolean vanish) {
		this.vanish = vanish;
	}

	@Override
	public void setTeamspeakId(int teamspeakId) {
		this.teamspeakId = teamspeakId;
	}

	@Override
	public int getTeamspeakId() {
		return teamspeakId;
	}

	@Override
	public long getFirstConnection() {
		return firstConnection;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}
}
