package fr.olympa.api.spigot.utils;

import java.text.DecimalFormat;
import java.util.Arrays;

import net.md_5.bungee.api.ChatColor;

public class TPSUtils {

	private static DecimalFormat format = new DecimalFormat("##.##");

	public static float getAverage(double[] tps) {
		return Math.round(Arrays.stream(tps).sum() / tps.length);
	}
	
	public static float round(double tps) {
		return (float) Math.min(Math.round(tps * 100.0) / 100.0, 20.0);
	}
	
	public static float[] round(double[] tps) {
		float[] tpsFloat = new float[tps.length];
		for (int i = 0; i < tps.length; i++) tpsFloat[i] = round(tps[i]);
		return tpsFloat;
	}
	
	// A ajuster
	public static String getCPUUsageColor(int cpuUsage) {
		if (cpuUsage < 25)
			return ChatColor.GREEN + String.valueOf(cpuUsage);
		else if (cpuUsage < 50)
			return ChatColor.GOLD + String.valueOf(cpuUsage);
		else
			return ChatColor.RED + String.valueOf(cpuUsage);
	}

	// A ajuster
	public static String getRamUsageColor(int ramUsage) {
		if (ramUsage < 50)
			return ChatColor.GREEN + String.valueOf(ramUsage);
		else if (ramUsage < 75)
			return ChatColor.GOLD + String.valueOf(ramUsage);
		else
			return ChatColor.RED + String.valueOf(ramUsage);
	}

	public static String getTpsColor(double tps) {
		if (tps >= 18)
			return ChatColor.GREEN + format.format(tps);
		else if (tps >= 16)
			return ChatColor.GOLD + format.format(tps);
		else
			return ChatColor.RED + format.format(tps);
	}

	public static String getPingColor(int ping) {
		if (ping < 50)
			return ChatColor.GREEN + String.valueOf(ping);
		else if (ping < 80)
			return ChatColor.GOLD + String.valueOf(ping);
		else
			return ChatColor.RED + String.valueOf(ping);
	}
}
