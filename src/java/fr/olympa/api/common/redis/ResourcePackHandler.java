package fr.olympa.api.common.redis;

public interface ResourcePackHandler {
	
	void handle(String playerName, String serverName, boolean set);
	
}
