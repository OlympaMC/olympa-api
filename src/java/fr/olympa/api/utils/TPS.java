package fr.olympa.api.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
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

	public static String getAllStringTPS() {
		return StringUtils.join(ArrayUtils.toObject(getAllTPS()), " ");
	}

	public static float[] getAllTPS() {
		double[] recentTps = getDoubleTPS();
		float[] recentTpsF = round(recentTps);
		return recentTpsF;
	}

	public static float getAverage() {
		return getAverage(getDoubleTPS());
	}

	public static float getAverage(double[] tps) {
		return round(Arrays.stream(tps).sum() / tps.length);
	}

	public static double[] getDoubleTPS() {
		double[] recentTps;
		// if (canGetWithPaper()) {
		// recentTps = getPaperRecentTps();
		// } else {
		recentTps = getNMSRecentTps();
		// }
		return recentTps;
	}

	public static int getIntTPS() {
		return getIntTPS(1);
	}

	public static int getIntTPS(int time) {
		return Math.round(getTPS(time));
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

	@SuppressWarnings("unused")
	private static double[] getPaperRecentTps() {
		if (!canGetWithPaper()) {
			throw new UnsupportedOperationException("Can't get TPS from Paper");
		}
		Object server = Reflection.callMethod(getTPSMethod, null); // Call static MinecraftServer.getServer()
		double[] recent = Reflection.getFieldValue(recentTpsField, server);
		return recent;
	}

	public static double getTPS() {
		return getTPS(1);
	}

	public static float getTPS(int time) {
		float[] recentTps = getAllTPS();
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

	public static float round(double tps) {
		return (float) Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
	}

	public static float[] round(double[] tps) {
		float[] tpsFloat = new float[tps.length];
		for (int i = 0; i < tps.length; i++) {
			tpsFloat[i] = round(tps[i]);
		}
		return tpsFloat;
	}
}
