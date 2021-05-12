package fr.olympa.api.utils;

import fr.olympa.api.utils.spigot.TPS;

public class SpigotInfo {

	private static Boolean isPapermc;
	private static Boolean isTunity;
	private static Boolean isPurpur;

	public static String getVersionBukkit() {
		return TPS.isSpigot() ? isPaper() ? isTunity() ? isPurpur() ? "Purpur" : "Tunity" : "PaperSpigot" : "Spigot" : "Bukkit";
	}

	private static boolean isTunity() {
		if (isTunity == null)
			try {
				isTunity = Class.forName("com.tuinity.tuinity.util$TickThread") != null;
			} catch (ClassNotFoundException e) {
				isTunity = false;
			}
		return isTunity;
	}

	private static boolean isPaper() {
		if (isPapermc == null)
			try {
				isPapermc = Class.forName("com.destroystokyo.paper.VersionHistoryManager$VersionData") != null;
			} catch (ClassNotFoundException e) {
				isPapermc = false;
			}
		return isPapermc;
	}

	private static boolean isPurpur() {
		if (isPurpur == null)
			try {
				isPurpur = Class.forName("net.pl3x.purpur.event$PlayerAFKEvent") != null;
			} catch (ClassNotFoundException e) {
				isPurpur = false;
			}
		return isPurpur;
	}
}
