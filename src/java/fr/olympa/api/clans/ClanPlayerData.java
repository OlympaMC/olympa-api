package fr.olympa.api.clans;

import fr.olympa.api.player.OlympaPlayerInformations;

public abstract class ClanPlayerData<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> {
	
	protected final OlympaPlayerInformations playerInformations;
	
	protected ClanPlayerInterface<T, D> player;
	
	public ClanPlayerData(OlympaPlayerInformations informations) {
		this.playerInformations = informations;
	}
	
	public void playerJoin(ClanPlayerInterface<T, D> player) {
		this.player = player;
	}
	
	public void playerLeaves() {
		this.player = null;
	}
	
	public OlympaPlayerInformations getPlayerInformations() {
		return playerInformations;
	}
	
	public boolean isConnected() {
		return player != null;
	}
	
	public ClanPlayerInterface<T, D> getConnectedPlayer() {
		return player;
	}
	
}
