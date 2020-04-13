package fr.olympa.api.region.shapes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.util.NumberConversions;

import fr.olympa.api.region.Region;

public class Cuboid implements Region, ConfigurationSerializable {

	protected final int xMin;
	protected final int xMax;
	protected final int yMin;
	protected final int yMax;
	protected final int zMin;
	protected final int zMax;
	protected final double xMinCentered;
	protected final double xMaxCentered;
	protected final double yMinCentered;
	protected final double yMaxCentered;
	protected final double zMinCentered;
	protected final double zMaxCentered;
	protected final World world;
	protected final Location center, min, max;

	public Cuboid(final Location point1, final Location point2) {
		this(point1.getWorld(), point1.getBlockX(), point1.getBlockY(), point1.getBlockZ(), point2.getBlockX(), point2.getBlockY(), point2.getBlockZ());
	}

	public Cuboid(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
		this.world = world;
		this.xMin = Math.min(x1, x2);
		this.xMax = Math.max(x1, x2);
		this.yMin = Math.min(y1, y2);
		this.yMax = Math.max(y1, y2);
		this.zMin = Math.min(z1, z2);
		this.zMax = Math.max(z1, z2);
		this.xMinCentered = this.xMin + 0.5;
		this.xMaxCentered = this.xMax + 0.5;
		this.yMinCentered = this.yMin + 0.5;
		this.yMaxCentered = this.yMax + 0.5;
		this.zMinCentered = this.zMin + 0.5;
		this.zMaxCentered = this.zMax + 0.5;
		this.center = new Location(this.world, (this.xMax - this.xMin) / 2 + this.xMin, (this.yMax - this.yMin) / 2 + this.yMin, (this.zMax - this.zMin) / 2 + this.zMin);
		this.min = new Location(world, xMin, yMin, zMin);
		this.max = new Location(world, xMax, yMax, zMax);
	}

	public Location getCenter() {
		return center;
	}

	@Override
	public Location getMin() {
		return min;
	}

	@Override
	public Location getMax() {
		return max;
	}

	@Override
	public World getWorld() {
		return world;
	}

	public double getDistance() {
		return Math.sqrt(getDistanceSquared());
	}

	public double getDistanceSquared() {
		return NumberConversions.square(xMax - xMin) + NumberConversions.square(yMax - yMin) + NumberConversions.square(zMax - zMin);
	}

	public int getHeight() {
		return this.yMax - this.yMin + 1;
	}

	public Iterator<Location> getPerimeterLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for(int x = this.xMin; x <= this.xMax; ++x) {
			for(int z = this.zMin; z <= this.zMax; ++z) {
				locations.add(new Location(this.world, x, this.yMin, z));
				locations.add(new Location(this.world, x, this.yMax, z));
			}

		}
		return locations.iterator();
	}

	public Location getPoint1() {
		return new Location(this.world, this.xMin, this.yMin, this.zMin);
	}

	public Location getPoint2() {
		return new Location(this.world, this.xMax, this.yMax, this.zMax);
	}

	@Override
	public Location getRandomLocation() {
		final Random rand = new Random();
		final int x = rand.nextInt(getXWidth()) + this.xMin;
		final int y = rand.nextInt(getHeight()) + this.yMin;
		final int z = rand.nextInt(getZWidth()) + this.zMin;
		return new Location(this.world, x, y, z);
	}

	public int getTotalBlockSize() {
		return this.getHeight() * this.getXWidth() * this.getZWidth();
	}

	public int getXWidth() {
		return this.xMax - this.xMin + 1;
	}

	public int getZWidth() {
		return this.zMax - this.zMin + 1;
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		return world == this.world && x >= this.xMin && x <= this.xMax && y >= this.yMin && y <= this.yMax && z >= this.zMin && z <= this.zMax;
	}

	public boolean isInWithMarge(final Location loc, final double marge) {
		return loc.getWorld() == this.world && loc.getX() >= this.xMinCentered - marge && loc.getX() <= this.xMaxCentered + marge && loc.getY() >= this.yMinCentered - marge && loc
				.getY() <= this.yMaxCentered + marge && loc.getZ() >= this.zMinCentered - marge && loc.getZ() <= this.zMaxCentered + marge;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("world", world.getName());
		map.put("xMin", xMin);
		map.put("xMax", xMax);
		map.put("yMin", yMin);
		map.put("yMax", yMax);
		map.put("zMin", zMin);
		map.put("zMax", zMax);
		return map;
	}

	public static Cuboid deserialize(Map<String, Object> map) {
		return new Cuboid(Bukkit.getWorld((String) map.get("world")), (int) map.get("xMin"), (int) map.get("yMin"), (int) map.get("zMin"), (int) map.get("xMax"), (int) map.get("yMax"), (int) map.get("zMax"));
	}

}
