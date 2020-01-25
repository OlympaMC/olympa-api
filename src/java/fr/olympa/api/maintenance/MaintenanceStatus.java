package fr.olympa.api.maintenance;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import exemple.OlympaAPIPermission;
import fr.olympa.api.permission.OlympaPermission;
import net.md_5.bungee.api.ChatColor;

public enum MaintenanceStatus {

	OPEN("Ouvert", ChatColor.GREEN, null, "off"),
	MAINTENANCE("Maintenance", ChatColor.RED, OlympaAPIPermission.CONNECT_SERVERSTATUS_MAINTENANCE, "on"),
	DEV("Développement", ChatColor.LIGHT_PURPLE, OlympaAPIPermission.CONNECT_SERVERSTATUS_DEV, "dev"),
	BETA("Beta", ChatColor.GOLD, OlympaAPIPermission.CONNECT_SERVERSTATUS_BETA, "beta"),
	SOON("Bientôt", ChatColor.YELLOW, OlympaAPIPermission.CONNECT_SERVERSTATUS_SOON, "soon"),
	CLOSE("Fermer", ChatColor.DARK_RED, null, null),
	UNKNOWN("Inconnu", ChatColor.RED, OlympaAPIPermission.CONNECT_SERVERSTATUS_DEV, null);

	public static MaintenanceStatus get(String name) {
		return Arrays.stream(values()).filter(status -> status.name.equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public static MaintenanceStatus getByCommandArg(String commandArg) {
		return Arrays.stream(values()).filter(status -> status.commandArg != null && status.commandArg.equalsIgnoreCase(commandArg)).findFirst().orElse(null);
	}

	public static List<String> getNames() {
		return Arrays.stream(values()).map(MaintenanceStatus::getName).collect(Collectors.toList());
	}

	private String name;
	private ChatColor color;
	private OlympaPermission permission;
	private String commandArg;

	private MaintenanceStatus(String name, ChatColor color, OlympaPermission permission, String commandArg) {
		this.name = name;
		this.color = color;
		this.permission = permission;
		this.commandArg = commandArg;
	}

	public ChatColor getColor() {
		return this.color;
	}

	public String getCommandArg() {
		return this.commandArg;
	}

	public String getName() {
		return this.name;
	}

	public String getNameColored() {
		return this.color + this.name;
	}

	public OlympaPermission getPermission() {
		return this.permission;
	}
}
