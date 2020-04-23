package fr.olympa.api.utils;

import java.text.DecimalFormat;

import net.md_5.bungee.api.ChatColor;

public class TPSUtils {

	private static DecimalFormat format = new DecimalFormat("##.##");
	public static String getColor(double tps) {
		if (tps >= 18) {
			return ChatColor.GREEN + format.format(tps);
		} else if (tps >= 16) {
			return ChatColor.GOLD + format.format(tps);
		} else {
			return ChatColor.RED + format.format(tps);
		}
	}
}
