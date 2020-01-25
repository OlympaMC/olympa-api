package fr.olympa.api.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import fr.olympa.api.utils.Reflection.ClassEnum;

public class TPS {

	private static final Class<?> spigotServerClass = Reflection.getClass("org.bukkit.Server$Spigot");
	private static final Method getSpigotMethod = Reflection.makeMethod(Bukkit.class, "spigot");
	private static final Method getTPSMethod = spigotServerClass != null ? Reflection.makeMethod(spigotServerClass, "getTPS") : null;
	private static final Class<?> minecraftServerClass = Reflection.getClass(ClassEnum.NMS, "MinecraftServer");
	private static final Method getServerMethod = minecraftServerClass != null ? Reflection.makeMethod(minecraftServerClass, "getServer") : null;
	private static final Field recentTpsField = minecraftServerClass != null ? Reflection.makeField(minecraftServerClass, "recentTps") : null;

	private static boolean canGetWithPaper() {
		return getSpigotMethod != null && getTPSMethod != null;
	}

	public static String getColor(final double tps) {
		if (tps >= 18) {
			return ChatColor.GREEN + String.valueOf(tps);
		} else if (tps >= 16) {
			return ChatColor.GOLD + String.valueOf(tps);
		} else {
			return ChatColor.RED + String.valueOf(tps);
		}
	}

	private static double[] getNMSRecentTps() {
		if (getServerMethod == null || recentTpsField == null) {
			try {
				throw new Exception("Can't get TPS from NMS");
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
		final Object server = Reflection.callMethod(getServerMethod, null); // Call static MinecraftServer.getServer()
		final double[] recent = Reflection.getFieldValue(recentTpsField, server);
		return recent;
	}

	private static double[] getPaperRecentTps() {
		if (!canGetWithPaper()) {
			throw new UnsupportedOperationException("Can't get TPS from Paper");
		}
		final Object server = Reflection.callMethod(getServerMethod, null); // Call static MinecraftServer.getServer()
		final double[] recent = Reflection.getFieldValue(recentTpsField, server);
		return recent;
	}

	public static double getTPS() {
		return getTPS(1);
	}

	public static double getTPS(final int time) {
		double[] recentTps;
		if (canGetWithPaper()) {
			recentTps = getPaperRecentTps();
		} else {
			recentTps = getNMSRecentTps();
		}
		double raw;
		double tps;
		switch (time) {
		case 1:
			raw = recentTps[0];
			tps = Math.min(Math.round(raw * 100.0) / 100.0, 20.0);
			return tps;
		case 5:
			raw = recentTps[1];
			tps = Math.min(Math.round(raw * 100.0) / 100.0, 20.0);
			return tps;
		case 15:
			raw = recentTps[2];
			tps = Math.min(Math.round(raw * 100.0) / 100.0, 20.0);
			return tps;
		default:
			throw new IllegalArgumentException("Unsupported tps measure time " + time);
		}
	}

	public static int getTPSint() {
		return getTPSint(1);
	}

	public static int getTPSint(final int time) {
		return (int) Math.round(getTPS(time));
	}
}
