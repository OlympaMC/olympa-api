package fr.olympa.api.clans;

import fr.olympa.api.player.OlympaPlayerInformations;

public class ClanPlayerData<T extends Clan<T>> {
	
	protected final OlympaPlayerInformations playerInformations;
	
	protected ClanPlayerInterface<T> player;
	
	public ClanPlayerData(OlympaPlayerInformations informations) {
		this.playerInformations = informations;
	}
	
	public void playerJoin(ClanPlayerInterface<T> player) {
		this.player = player;
	}
	
	public void playerLeaves() {
		this.player = null;
	}
	
	public OlympaPlayerInformations getPlayerInformations() {
		return playerInformations;
	}
	
	public boolean isConnected() {
		return player == null;
	}
	
	public ClanPlayerInterface<T> getConnectedPlayer() {
		return player;
	}
	
}
