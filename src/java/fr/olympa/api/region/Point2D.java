package fr.olympa.api.region;

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

}
