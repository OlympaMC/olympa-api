package fr.olympa.api.server;

import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;

public enum OlympaServer {

	ALL(null, false),
	BUNGEE("BungeeCord", true, ServerType.BUNGEE),
	AUTH("Authentification", true),
	LOBBY("Lobby", true),
	DEV("Développement", false, OlympaAPIPermissions.CONNECT_SERVER_DEV),
	BUILDEUR("Buildeur", false, OlympaAPIPermissions.CONNECT_SERVER_BUILDER),
	ZTA("Olympa ZTA", false),
	CREATIF("Créatif", false),
	LG("Loup-Garou", true),
	PVPFAC("PvP-Factions", false);

	private final String name;
	private final boolean multi;
	private OlympaPermission joinPermission;
	private ServerType type = ServerType.SPIGOT;

	private OlympaServer(String name, boolean multi) {
		this.name = name;
		this.multi = multi;
	}

	private OlympaServer(String name, boolean multi, ServerType type) {
		this(name, multi);
		this.type = type;
	}

	private OlympaServer(String name, boolean multi, OlympaPermission joinPermission) {
		this(name, multi);
		this.joinPermission = joinPermission;
	}

	public String getNameCaps() {
		return name;
	}

	public boolean hasMultiServers() {
		return multi;
	}

	public OlympaPermission getJoinPermission() {
		return joinPermission;
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
