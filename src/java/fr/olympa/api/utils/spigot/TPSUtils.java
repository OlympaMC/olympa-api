package fr.olympa.api.utils.spigot;

import java.text.DecimalFormat;

import net.md_5.bungee.api.ChatColor;

public class TPSUtils {

	private static DecimalFormat format = new DecimalFormat("##.##");

	// A ajuster
	public static String getCPUUsageColor(int cpuUsage) {
		if (cpuUsage < 25) {
			return ChatColor.GREEN + String.valueOf(cpuUsage);
		} else if (cpuUsage < 50) {
			return ChatColor.GOLD + String.valueOf(cpuUsage);
		} else {
			return ChatColor.RED + String.valueOf(cpuUsage);
		}
	}

	// A ajuster
	public static String getRamUsageColor(int ramUsage) {
		if (ramUsage < 50) {
			return ChatColor.GREEN + String.valueOf(ramUsage);
		} else if (ramUsage < 75) {
			return ChatColor.GOLD + String.valueOf(ramUsage);
		} else {
			return ChatColor.RED + String.valueOf(ramUsage);
		}
	}

	public static String getTpsColor(double tps) {
		if (tps >= 18) {
			return ChatColor.GREEN + format.format(tps);
		} else if (tps >= 16) {
			return ChatColor.GOLD + format.format(tps);
		} else {
			return ChatColor.RED + format.format(tps);
		}
	}
}
