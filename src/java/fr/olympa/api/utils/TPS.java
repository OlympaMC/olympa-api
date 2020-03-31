package fr.olympa.api.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.Bukkit;

import fr.olympa.api.utils.Reflection.ClassEnum;

public class TPS {

	private static Class<?> spigotServerClass = Reflection.getClass("org.bukkit.Server$Spigot");
	private static Method getSpigotMethod = Reflection.makeMethod(Bukkit.class, "spigot");
	private static Method getTPSMethod = spigotServerClass != null ? Reflection.makeMethod(spigotServerClass, "getTPS") : null;
	private static Class<?> minecraftServerClass = Reflection.getClass(ClassEnum.NMS, "MinecraftServer");
	private static Method getServerMethod = minecraftServerClass != null ? Reflection.makeMethod(minecraftServerClass, "getServer") : null;
	private static Field recentTpsField = minecraftServerClass != null ? Reflection.makeField(minecraftServerClass, "recentTps") : null;

	private static boolean canGetWithPaper() {
		return getSpigotMethod != null && getTPSMethod != null;
	}

	private static double[] getNMSRecentTps() {
		if (getServerMethod == null || recentTpsField == null) {
			try {
				throw new Exception("Can't get TPS from NMS");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Object server = Reflection.callMethod(getServerMethod, null); // Call static MinecraftServer.getServer()
		double[] recent = Reflection.getFieldValue(recentTpsField, server);
		return recent;
	}

	private static double[] getPaperRecentTps() {
		if (!canGetWithPaper()) {
			throw new UnsupportedOperationException("Can't get TPS from Paper");
		}
		Object server = Reflection.callMethod(getServerMethod, null); // Call static MinecraftServer.getServer()
		double[] recent = Reflection.getFieldValue(recentTpsField, server);
		return recent;
	}

	public static double getTPS() {
		return getTPS(1);
	}

	public static double getTPS(int time) {
		double[] recentTps = getTPSs();
		switch (time) {
		case 1:
			return recentTps[0];
		case 5:
			return recentTps[1];
		case 15:
			return recentTps[2];
		default:
			throw new IllegalArgumentException("Unsupported tps measure time " + time);
		}
	}

	public static int getTPSint() {
		return getTPSint(1);
	}

	public static int getTPSint(int time) {
		return (int) Math.round(getTPS(time));
	}

	public static double[] getTPSs() {
		double[] recentTps;
		if (canGetWithPaper()) {
			recentTps = getPaperRecentTps();
		} else {
			recentTps = getNMSRecentTps();
		}
		for (int i = 0; i < recentTps.length; i++) {
			recentTps[i] = Math.min(Math.round(recentTps[i] * 100.0) / 100.0, 20.0);
		}
		return recentTps;
	}
}
