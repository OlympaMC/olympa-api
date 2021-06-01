package fr.olympa.api.common.bpmc;

/**
 * @Deprecated Don't use it, use Redis channels instead
 * https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/ is unstable and need at least 1 player connected.
 */
@Deprecated
public class PlayerCountResponse {

	final String serverName;
	final int playerCount;

	public PlayerCountResponse(String serverName, int playerCount) {
		this.serverName = serverName;
		this.playerCount = playerCount;
	}

	public int getPlayerCount() {
		return playerCount;
	}

	public String getServerName() {
		return serverName;
	}
}
