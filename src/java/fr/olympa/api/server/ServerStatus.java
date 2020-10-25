package fr.olympa.api.server;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaPermission;
import net.md_5.bungee.api.ChatColor;

public enum ServerStatus {

	OPEN(1, "Ouvert", ChatColor.GREEN, null, "off"),
	SOON(2, "Bientôt", ChatColor.YELLOW, OlympaAPIPermissions.CONNECT_SERVERSTATUS_SOON, "soon"),
	BETA(3, "Bêta", ChatColor.GOLD, OlympaAPIPermissions.CONNECT_SERVERSTATUS_BETA, "beta"),
	CLOSE_BETA(7, "Bêta Fermer", ChatColor.GOLD, OlympaAPIPermissions.CONNECT_SERVERSTATUS_BETA, "beta"),
	MAINTENANCE(4, "Maintenance", ChatColor.RED, OlympaAPIPermissions.CONNECT_SERVERSTATUS_MAINTENANCE, "on"),
	DEV(5, "Développement", ChatColor.LIGHT_PURPLE, OlympaAPIPermissions.CONNECT_SERVERSTATUS_DEV, "dev"),
	UNKNOWN(6, "Inconnu", ChatColor.DARK_RED, OlympaAPIPermissions.CONNECT_SERVERSTATUS_DEV, null),
	CLOSE(10, "Fermé", ChatColor.RED, null, null);

	public static ServerStatus get(int id) {
		return Arrays.stream(values()).filter(status -> status.getId() == id).findFirst().orElse(ServerStatus.UNKNOWN);
	}

	public static ServerStatus get(String name) {
		return Arrays.stream(values()).filter(status -> status.getName().equalsIgnoreCase(name)).findFirst().orElse(ServerStatus.UNKNOWN);
	}

	public static ServerStatus getByCommandArg(String commandArg) {
		return Arrays.stream(values()).filter(status -> status.getCommandArg() != null && status.commandArg.equalsIgnoreCase(commandArg)).findFirst().orElse(null);
	}

	public static List<String> getNames() {
		return Arrays.stream(values()).filter(status -> status != UNKNOWN).map(ServerStatus::getName).collect(Collectors.toList());
	}

	private int id;
	private String name;

	private ChatColor color;
	private OlympaPermission permission;
	private String commandArg;

	private ServerStatus(int id, String name, ChatColor color, OlympaPermission permission, String commandArg) {
		this.id = id;
		this.name = name;
		this.color = color;
		this.permission = permission;
		this.commandArg = commandArg;
	}

	public ChatColor getColor() {
		return color;
	}

	public String getCommandArg() {
		return commandArg;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getNameColored() {
		return color + name;
	}

	public OlympaPermission getPermission() {
		return permission;
	}

	public boolean canConnect() {
		return this != CLOSE && this != UNKNOWN;
	}
}
