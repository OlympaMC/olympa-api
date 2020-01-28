package fr.olympa.api.bpmc;

public class PlayerCountResponse {

	final String serverName;
	final int playerCount;

	public PlayerCountResponse(String serverName, int playerCount) {
		this.serverName = serverName;
		this.playerCount = playerCount;
	}

	public int getPlayerCount() {
		return this.playerCount;
	}

	public String getServerName() {
		return this.serverName;
	}
}
