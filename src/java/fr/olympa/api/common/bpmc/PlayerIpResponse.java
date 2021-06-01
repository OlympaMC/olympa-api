package fr.olympa.api.common.bpmc;

/**
 * @Deprecated Don't use it, use Redis channels instead
 * https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/ is unstable and need at least 1 player connected.
 */
@Deprecated
public class PlayerIpResponse {

	final String ip;
	final int port;

	public PlayerIpResponse(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return ip;
	}

	public int getPort() {
		return port;
	}
}
