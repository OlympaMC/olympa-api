package fr.olympa.api.bungee.player;

import java.util.UUID;

import fr.olympa.api.common.player.OlympaPlayer;
import net.md_5.bungee.api.connection.PendingConnection;

public class CachePlayer {

	UUID premiumUUID;
	String name;
	UUID uuid;
	OlympaPlayer olympaPlayer;
	String subDomain;

	public CachePlayer(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	public UUID getPremiumUUID() {
		return premiumUUID;
	}

	public String getSubDomain() {
		return subDomain;
	}

	public void setOlympaPlayer(OlympaPlayer olympaPlayer) {
		this.olympaPlayer = olympaPlayer;
	}

	public void setPremiumUUID(UUID premiumUUID) {
		this.premiumUUID = premiumUUID;
	}

	public void setSubDomain(PendingConnection connection) {
		setSubDomain(connection.getVirtualHost().getHostName().split("\\.")[0]);
	}

	public void setSubDomain(String subDomain) {
		this.subDomain = subDomain;
	}
}
