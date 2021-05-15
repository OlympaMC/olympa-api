package fr.olympa.api.server;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;

public enum OlympaServer {

	ALL(null, false),
	BUNGEE("BungeeCord", true, ServerType.BUNGEE),
	AUTH("Authentification", true),
	LOBBY("Lobby", true),
	DEV("Développement", false),
	BUILDEUR("Buildeur", false),
	ZTA("Olympa ZTA", false, true),
	CREATIF("Créatif", false),
	LG("Loup-Garou", true),
	PVPFAC("PvP-Factions", false),
	PVPKIT("PvP-Kits", false),
	;

	private final String name;
	private final boolean multi;
	private OlympaPermission joinPermission;
	private ServerType type = ServerType.SPIGOT;
	private boolean hasPack;

	private OlympaServer(String name, boolean multi) {
		this(name, multi, false);
	}
	
	private OlympaServer(String name, boolean multi, boolean hasPack) {
		this.name = name;
		this.multi = multi;
		this.hasPack = hasPack;
	}

	private OlympaServer(String name, boolean multi, ServerType type) {
		this(name, multi);
		this.type = type;
	}

	public String getNameCaps() {
		return name;
	}

	public boolean hasMultiServers() {
		return multi;
	}
	
	public boolean hasPack() {
		return hasPack;
	}

	public OlympaPermission getJoinPermission() {
		return joinPermission;
	}
	
	public void setJoinPermission(OlympaPermission joinPermission) {
		this.joinPermission = joinPermission;
	}

	public boolean canConnect(OlympaPlayer player) {
		return joinPermission == null || joinPermission.hasPermission(player);
	}

	public ServerType getType() {
		return type;
	}

	public boolean isSame(OlympaServer olympaServer) {
		return getNameCaps().equals(olympaServer.getNameCaps());
	}
}
