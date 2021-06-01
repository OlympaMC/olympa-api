package fr.olympa.api.common.server;

import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.player.OlympaPlayer;

public enum OlympaServer {

	ALL(null, false),
	BUNGEE("BungeeCord", true, ServerFrameworkType.BUNGEE),
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
	private ServerFrameworkType type = ServerFrameworkType.SPIGOT;
	private boolean hasPack;

	OlympaServer(String name, boolean multi) {
		this(name, multi, false);
	}

	OlympaServer(String name, boolean multi, boolean hasPack) {
		this.name = name;
		this.multi = multi;
		this.hasPack = hasPack;
	}

	OlympaServer(String name, boolean multi, ServerFrameworkType type) {
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

	public ServerFrameworkType getType() {
		return type;
	}

	public boolean isSame(OlympaServer olympaServer) {
		return ordinal() == olympaServer.ordinal();
	}
}
