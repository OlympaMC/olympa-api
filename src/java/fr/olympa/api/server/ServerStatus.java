package fr.olympa.api.server;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.permission.OlympaGlobalPermission;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.permission.list.OlympaAPIPermissionsGlobal;
import net.md_5.bungee.api.ChatColor;

public enum ServerStatus {

	OPEN(1, "Ouvert", ChatColor.GREEN, null, "Off"),
	SOON(2, "Bientôt", ChatColor.YELLOW, OlympaAPIPermissionsGlobal.CONNECT_SERVERSTATUS_SOON, "Bientôt"),
	BETA(3, "Bêta", ChatColor.GOLD, OlympaAPIPermissionsGlobal.CONNECT_SERVERSTATUS_BETA, "Bêta"),
	CLOSE_BETA(7, "Bêta Fermée", ChatColor.GOLD, OlympaAPIPermissionsGlobal.CONNECT_SERVERSTATUS_BETA, "Bêta Fermée"),
	MAINTENANCE(4, "Maintenance", ChatColor.RED, OlympaAPIPermissionsGlobal.CONNECT_SERVERSTATUS_MAINTENANCE, "On"),
	DEV(5, "Développement", ChatColor.LIGHT_PURPLE, OlympaAPIPermissionsGlobal.CONNECT_SERVERSTATUS_DEV, "Dev"),
	UNKNOWN(6, "Inconnu", ChatColor.DARK_RED, OlympaAPIPermissionsGlobal.CONNECT_SERVERSTATUS_DEV, null),
	STARTING(8, "Démarrage", ChatColor.BLUE, OlympaAPIPermissionsGlobal.CONNECT_SERVERSTATUS_MAINTENANCE, null),
	CLOSE(10, "Fermé", ChatColor.RED, null, null);

	public static ServerStatus get(int id) {
		return Arrays.stream(values()).filter(status -> status.getId() == id).findFirst().orElse(ServerStatus.UNKNOWN);
	}

	public static ServerStatus get(String name) {
		return Arrays.stream(values()).filter(status -> status.getName().equalsIgnoreCase(name) || status.name().equalsIgnoreCase(name)).findFirst().orElse(ServerStatus.UNKNOWN);
	}

	public static ServerStatus getByCommandArg(String commandArg) {
		return Arrays.stream(values()).filter(status -> status.getCommandArg() != null && status.getCommandArg().equalsIgnoreCase(commandArg)).findFirst().orElse(null);
	}

	public static List<String> getNames() {
		return Arrays.stream(values()).filter(status -> status != UNKNOWN).map(ServerStatus::getName).collect(Collectors.toList());
	}

	public static List<String> getCommandsArgs() {
		return Arrays.stream(values()).filter(status -> status.getCommandArg() != null).map(ServerStatus::getCommandArg).collect(Collectors.toList());
	}

	private int id;
	private String name;

	private ChatColor color;
	private OlympaPermission permission;
	private String commandArg;

	ServerStatus(int id, String name, ChatColor color, OlympaGlobalPermission permission, String commandArg) {
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
