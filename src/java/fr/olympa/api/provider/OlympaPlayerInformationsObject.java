package fr.olympa.api.provider;

import java.util.UUID;

import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.player.OlympaPlayerInformations;

public class OlympaPlayerInformationsObject implements OlympaPlayerInformations {
	
	private long id;
	private String name;
	private UUID uuid;
	private ClanPlayerData clanData;

	public void setClanData(ClanPlayerData clanData) {
		this.clanData = clanData;
	}

	public OlympaPlayerInformationsObject(long id, String name, UUID uuid) {
		this.id = id;
		this.name = name;
		this.uuid = uuid;
	}
	
	@Override
	public long getId() {
		return id;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public UUID getUUID() {
		return uuid;
	}
	
	@Override
	public ClanPlayerData getClanData() {
		return clanData;
	}
	
}
