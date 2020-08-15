package fr.olympa.api.utils;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

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

	public Point2D(Chunk chunk) {
		this.x = chunk.getX();
		this.z = chunk.getZ();
	}
	
	public int getX() {
		return x;
	}
	
	public int getZ() {
		return z;
	}
	
	public Chunk asChunk(World world) {
		return world.getChunkAt(x, z);
	}
	
	public String toString() {
		return x + "|" + z;
	}

	@Override
	public int hashCode() {
		long bits = x;
		bits ^= z * 31;
		return (((int) bits) ^ ((int) (bits >> 32)));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) return true;
		if (obj instanceof Point2D) {
			Point2D other = (Point2D) obj;
			return other.x == x && other.z == z;
		}
		return false;
	}

	public static Point2D fromString(String string) {
		int index = string.indexOf('|');
		return new Point2D(Integer.parseInt(string.substring(0, index)), Integer.parseInt(string.substring(index + 1)));
	}

}
