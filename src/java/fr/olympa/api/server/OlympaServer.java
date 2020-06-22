package fr.olympa.api.server;

public enum OlympaServer {
	
	ALL(null),
	BUNGEE("BungeeCord", true),
	AUTH("Authentification", true),
	LOBBY("Lobby", true),
	DEV("Développement"),
	BUILDEUR("Buildeur"),
	ZTA("Olympa ZTA"),
	CREATIF("Créatif"),
	LG("Loup-Garou", true),
	PVPFAC("PvP-Factions");
	
	private final String name;
	private final boolean multi;
	
	private OlympaServer(String name) {
		this(name, false);
	}
	
	private OlympaServer(String name, boolean multi) {
		this.name = name;
		this.multi = multi;
	}
	
	public String getNameCaps() {
		return name;
	}
	
	public boolean hasMultiServers() {
		return multi;
	}
	
}
