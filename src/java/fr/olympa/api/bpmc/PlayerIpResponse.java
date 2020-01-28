package fr.olympa.api.bpmc;

public class PlayerIpResponse {

	final String ip;
	final int port;

	public PlayerIpResponse(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String getIp() {
		return this.ip;
	}

	public int getPort() {
		return this.port;
	}
}
