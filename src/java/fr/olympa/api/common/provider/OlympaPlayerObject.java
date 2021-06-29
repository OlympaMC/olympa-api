package fr.olympa.api.common.provider;

import java.util.Map;
import java.util.UUID;

import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.server.OlympaServer;

public class OlympaPlayerObject extends OlympaPlayerCore {

	public OlympaPlayerObject(UUID uuid, String name, String ip) {
		super(uuid, name, ip);
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
	public String getEmail() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<Long, String> getHistIp() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Map<String, OlympaServer> getCustomPermissions() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPassword() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isPremium() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isSamePassword(String password) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void loadSavedDatas(long id, UUID premiumUuid, String groupsString, long firstConnection, long lastConnection, String password, String email, Gender gender, String histNameJson, String histIpJson, int teamspeakId, boolean vanish) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setEmail(String email) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setIp(String ip) {
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
	public boolean hasCustomPermission(String permission, OlympaServer serv) {
		// TODO Auto-generated method stub
		return false;
	}

}
