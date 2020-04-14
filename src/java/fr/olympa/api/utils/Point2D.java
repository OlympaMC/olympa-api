package fr.olympa.api.utils;

import org.bukkit.Location;

public class Point2D {

	public final int x, z;

	public Point2D(int x, int z) {
		this.x = x;
		this.z = z;
	}

	public Point2D(Location loc) {
		this.x = loc.getBlockX();
		this.z = loc.getBlockZ();
	}

	public String toString() {
		return x + "|" + z;
	}

	public static Point2D fromString(String string) {
		int index = string.indexOf('|');
		return new Point2D(Integer.parseInt(string.substring(0, index)), Integer.parseInt(string.substring(index + 1)));
	}

}
