package fr.olympa.api.provider;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.Gender;
import fr.olympa.api.objects.OlympaMoney;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;

public class OlympaPlayerObject implements OlympaPlayer {

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson) {

	}

	@Override
	public void loadDatas(ResultSet resultSet) {}

	@Override
	public void saveDatas(PreparedStatement statement) {}

	@Override
	public void addGroup(OlympaGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OlympaGroup getGroup() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TreeMap<OlympaGroup, Long> getGroups() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGroupsToHumainString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getGroupsToString() {
		// TODO Auto-generated method stub
		return null;
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
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getIp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getLastConnection() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public OlympaMoney getStoreMoney() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Player getPlayer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getPremiumUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public UUID getUniqueId() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasPermission(OlympaPermission permission) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isAfk() {
		// TODO Auto-generated method stub
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
	public boolean isVerifMode() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAfk(boolean afk) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGender(Gender gender) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGroup(OlympaGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setGroup(OlympaGroup group, long time) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setId(long id) {
		// TODO Auto-generated method stub

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
		// TODO Auto-generated method stub

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
	public void setVerifMode(boolean verifMode) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isPremium() {
		return false;
	}

}
