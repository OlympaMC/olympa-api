package fr.olympa.api.common.server;

import java.util.AbstractMap;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import fr.olympa.api.common.match.MatcherPattern;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;

public enum OlympaServer {

	ALL(null, false),
	BUNGEE("BungeeCord", true, ServerFrameworkType.BUNGEE),
	ZTA("Olympa ZTA", false, true),
	CREATIF("Créatif", false),
	PVPKIT("PvP-Kits", false),
	PVPFAC("PvP-Factions", false),
	LG("Loup-Garou", true),
	LOBBY("Lobby", true),
	AUTH("Authentification", true),
	BUILDEUR("Buildeur", false),
	DEV("Développement", false),
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

	@Nullable
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

	public static Entry<OlympaServer, Integer> getOlympaServerWithId(String serverName) {
		java.util.regex.Matcher matcher = MatcherPattern.of("\\d*$").getPattern().matcher(serverName);
		matcher.find();
		String id = matcher.group();
		int serverID = Utils.isEmpty(id) ? 0 : Integer.parseInt(id);
		OlympaServer olympaServer = OlympaServer.valueOf(matcher.replaceAll("").toUpperCase());
		return new AbstractMap.SimpleEntry<>(olympaServer, serverID);
	}
}
