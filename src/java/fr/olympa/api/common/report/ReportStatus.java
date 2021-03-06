package fr.olympa.api.common.report;

import java.util.Arrays;

import net.md_5.bungee.api.ChatColor;

public enum ReportStatus {

	OPEN("Ouvert", ChatColor.GREEN),
	TOWATCH("Observation", ChatColor.LIGHT_PURPLE),
	WANTED("Recherché", ChatColor.GOLD),
	REFUSE("Refusé", ChatColor.RED),
	AGREE("Accepté", ChatColor.DARK_GREEN),
	AUTO("Automatique", ChatColor.AQUA);

	String name;
	ChatColor color;

	ReportStatus(String name, ChatColor color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public String getNameColored() {
		return color + name;
	}

	public ChatColor getColor() {
		return color;
	}

	public static ReportStatus get(String name) {
		return Arrays.stream(ReportStatus.values()).filter(r -> r.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}
}
