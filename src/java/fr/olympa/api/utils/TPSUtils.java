package fr.olympa.api.utils;

import net.md_5.bungee.api.ChatColor;

public class TPSUtils {

	public static String getColor(double tps) {
		if (tps >= 18) {
			return ChatColor.GREEN + String.valueOf(tps).replace(".0", "");
		} else if (tps >= 16) {
			return ChatColor.GOLD + String.valueOf(tps).replace(".0", "");
		} else {
			return ChatColor.RED + String.valueOf(tps).replace(".0", "");
		}
	}
}
