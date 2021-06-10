package fr.olympa.api.common.provider;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.utils.Utils;

public class OlympaPlayerObject implements OlympaPlayer {

	private UUID uuid;
	private String name;
	private String ip;
	private long id;
	private TreeMap<OlympaGroup, Long> groups = new TreeMap<>(Comparator.comparing(OlympaGroup::getPower).reversed());
	private Gender gender = Gender.MALE;

	private OlympaPlayerInformations cachedInfos = null;

	private Player cachedPlayer = null;

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
		groups.put(OlympaGroup.PLAYER, 0L);
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
	public void addNewIp(String ip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addNewName(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public OlympaPlayer clone() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getFirstConnection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Gender getGender() {
		return gender;
	}

	@Override
	public OlympaGroup getGroup() {
		return groups.firstKey();
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
			String time = new String();
			if (entry.getValue() != 0)
				time = " (" + Utils.timestampToDateAndHour(entry.getValue()) + ")";
			return entry.getKey().getName(gender) + time;
		}).collect(Collectors.joining(", "));
	}

	@Override
	public String getGroupsToString() {
		return groups.entrySet().stream().map(entry -> entry.getKey().getId() + (entry.getValue() != 0 ? ":" + entry.getValue() : "")).collect(Collectors.joining(";"));
	}

	@Override
	public TreeMap<Long, String> getHistHame() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<Long, String> getHistIp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public OlympaPlayerInformations getInformation() {
		return cachedInfos == null ? cachedInfos = AccountProviderAPI.getter().getPlayerInformations(id) : cachedInfos;
	}

	@Override
	public String getIp() {
		return ip;
	}

	@Override
	public long getLastConnection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getPlayer() {
		return cachedPlayer == null ? cachedPlayer = Bukkit.getPlayer(uuid) : cachedPlayer;
	}

	@Override
	public UUID getPremiumUniqueId() {
		return uuid;
	}

	@Override
	public String getTuneChar() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getUniqueId() {
		return uuid;
	}

	@Override
	public boolean isConnected() {
		return true;
	}

	@Override
	public boolean isGenderFemale() {
		return gender == Gender.FEMALE;
	}

	@Override
	public boolean isPremium() {
		return false;
	}

	@Override
	public boolean isSamePassword(String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isVanish() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadDatas(ResultSet resultSet) throws SQLException {}

	@Override
	public void loaded() {}

	@Override
	public void unloaded() {}

	//	@Override
	//	public void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson, int discordOlympaId, int teamspeakId, boolean vanish) {
	//	}

	@Override
	public void removeGroup(OlympaGroup group) {
		groups.remove(group);
	}

	@Override
	public void setConnected(boolean connected) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEmail(String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGender(Gender gender) {
		this.gender = gender;
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
	public void setId(long id) {
		this.id = id;
	}

	@Override
	public void setIp(String ip) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setLastConnection(long lastConnection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setPassword(String password) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setPremiumUniqueId(UUID premium_uuid) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setVanish(boolean vanish) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setTeamspeakId(int teamspeakId) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getTeamspeakId() {
		// TODO Auto-generated method stub
		return 0;
	}
	//
	//	@Override
	//	public int getDiscordOlympaId() {
	//		// TODO Auto-generated method stub
	//		return 0;
	//	}
	//
	//	@Override
	//	public void setDiscordOlympaId(int discordOlympaId) {
	//		// TODO Auto-generated method stub
	//
	//	}

	@Override
	public void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson, int teamspeakId, boolean vanish) {
		// TODO Auto-generated method stub

	}

	@Override
	public Map<String, OlympaServer> getCustomPermissions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Long, String> getHistName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasCustomPermission(String per, OlympaServer serv) {
		// TODO Auto-generated method stub
		return false;
	}

}
