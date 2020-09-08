package fr.olympa.api.report;

import net.md_5.bungee.api.ChatColor;

public enum ReportStatus {

	OPEN("Ouvert", ChatColor.GREEN),
	TOWATCH("À Observer", ChatColor.LIGHT_PURPLE),
	REFUSE("Refusé", ChatColor.RED);

	String name;
	ChatColor color;

	private ReportStatus(String name, ChatColor color) {
		this.name = name;
		this.color = color;
	}
}
