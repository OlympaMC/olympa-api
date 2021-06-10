package fr.olympa.api.common.bpmc;

/**
 * @Deprecated Don't use it, use Redis channels instead
 * https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/ is unstable and need at least 1 player connected.
 */
@Deprecated
public class PlayerListResponse {

	final String server;
	final String[] playerNames;

	public PlayerListResponse(String server, String[] playerNames) {
		this.server = server;
		this.playerNames = playerNames;
	}

	public String[] getPlayerNames() {
		return playerNames;
	}

	public String getServer() {
		return server;
	}
}
