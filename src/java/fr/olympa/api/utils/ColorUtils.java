package fr.olympa.api.utils;

import java.util.List;
import java.util.stream.Collectors;

import net.md_5.bungee.api.ChatColor;

public class ColorUtils {

	public static List<String> color(List<String> l) {
		return l.stream().map(s -> ColorUtils.color(s)).collect(Collectors.toList());
	}

	public static String color(String string) {
		return string != null ? ChatColor.translateAlternateColorCodes('&', string) : null;
	}
}
