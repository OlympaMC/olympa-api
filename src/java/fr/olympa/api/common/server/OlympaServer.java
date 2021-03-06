package fr.olympa.api.common.server;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.match.MatcherPattern;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.utils.Utils;

public enum OlympaServer {

	BUNGEE("BungeeCord", true, ServerFrameworkType.BUNGEE),
	ZTA("Olympa ZTA", false, false, true),
	CREATIF("Créatif", false),
	PVPKIT("PvP-Kits", false),
	PVPFAC("PvP-Factions", false),
	WARFARE("Warfare", true, false, false),
	LG("Loup-Garou", true),
	LOBBY("Lobby", true, true, true),
	AUTH("Authentification", true, true, true),
	BUILDEUR("Buildeur", false),
	DEV("Développement", true),
	ALL(null, false),
	;

	private final String name;
	private final boolean multi;
	private final boolean sendOther;
	private final boolean hasPack;
	private OlympaPermission joinPermission;
	private ServerFrameworkType type = ServerFrameworkType.SPIGOT;

	OlympaServer(String name, boolean multi) {
		this(name, multi, false, false);
	}

	OlympaServer(String name, boolean multi, boolean sendOther, boolean hasPack) {
		this.name = name;
		this.multi = multi;
		this.sendOther = sendOther;
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
	
	public boolean sendToOtherServer() {
		return sendOther;
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

	public boolean isSame(OlympaServer... olympaServers) {
		return Arrays.stream(olympaServers).anyMatch(olympaServer -> ordinal() == olympaServer.ordinal());
	}

	public static Integer getServerId(String serverName) {
		java.util.regex.Matcher matcher = MatcherPattern.of("\\d*$").getPattern().matcher(serverName);
		matcher.find();
		String id = matcher.group();
		return Utils.isEmpty(id) ? null : Integer.parseInt(id);
	}

	public static Entry<OlympaServer, Integer> getOlympaServerWithId(String serverName) {
		java.util.regex.Matcher matcher = MatcherPattern.of("\\d*$").getPattern().matcher(serverName);
		matcher.find();
		String id = matcher.group();
		int serverId = Utils.isEmpty(id) ? 0 : Integer.parseInt(id);
		String olympaServerName = matcher.replaceAll("").toUpperCase();
		OlympaServer olympaServer;
		try {
			olympaServer = OlympaServer.valueOf(olympaServerName);
		}catch (IllegalArgumentException ex) {
			olympaServer = null;
			LinkSpigotBungee.getInstance().sendMessage("§cOlympaServer.%s introuvable.", olympaServerName);
		}
		return new AbstractMap.SimpleEntry<>(olympaServer, serverId);
	}
}
