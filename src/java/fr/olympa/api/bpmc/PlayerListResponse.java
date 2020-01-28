package fr.olympa.api.bpmc;

public class PlayerListResponse {

	final String server;
	final String[] playerNames;

	public PlayerListResponse(String server, String[] playerNames) {
		this.server = server;
		this.playerNames = playerNames;
	}

	public String[] getPlayerNames() {
		return this.playerNames;
	}

	public String getServer() {
		return this.server;
	}
}
