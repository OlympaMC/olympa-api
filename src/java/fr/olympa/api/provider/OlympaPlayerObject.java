package fr.olympa.api.provider;

import java.util.TreeMap;
import java.util.UUID;

import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.permission.OlympaPermission;

public class OlympaPlayerObject implements OlympaPlayer {

	private UUID uuid;
	private String name;
	private String ip;

	public OlympaPlayerObject(int int1, UUID fromString, String string, String string2, String string3, long l, long m, String string4, String string5) {
		// TODO Auto-generated constructor stub
	}

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		this.uuid = uuid;
		this.name = name;
		this.ip = ip;
	}

	@Override
	public void addGroup(OlympaGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addGroup(OlympaGroup group, long time) {
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
	public int getId() {
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
	public boolean hasPower(OlympaGroup group2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasPower(OlympaGroup[] groups2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSamePassword(String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSamePower(OlympaGroup group2) {
		// TODO Auto-generated method stub
		return false;
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
	public void setGroupsFromString(String groupsString) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setId(int id) {
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
	public void setUniqueId(UUID uuid) {
		// TODO Auto-generated method stub

	}

}
