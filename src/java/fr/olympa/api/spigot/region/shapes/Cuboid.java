package fr.olympa.api.spigot.region.shapes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

import fr.olympa.api.spigot.region.AbstractRegion;

public class Cuboid extends AbstractRegion {

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
		xMin = Math.min(x1, x2);
		xMax = Math.max(x1, x2);
		yMin = Math.min(y1, y2);
		yMax = Math.max(y1, y2);
		zMin = Math.min(z1, z2);
		zMax = Math.max(z1, z2);
		xMinCentered = xMin + 0.5;
		xMaxCentered = xMax + 0.5;
		yMinCentered = yMin + 0.5;
		yMaxCentered = yMax + 0.5;
		zMinCentered = zMin + 0.5;
		zMaxCentered = zMax + 0.5;
		center = new Location(this.world, (xMax - xMin) / 2 + xMin, (yMax - yMin) / 2 + yMin, (zMax - zMin) / 2 + zMin);
		min = new Location(world, xMin, yMin, zMin);
		max = new Location(world, xMax, yMax, zMax);
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
		return yMax - yMin + 1;
	}

	public List<Location> getPerimeterLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (int x = xMin; x <= xMax; ++x)
			for (int z = zMin; z <= zMax; ++z) {
				locations.add(new Location(world, x, yMin, z));
				locations.add(new Location(world, x, yMax, z));
			}
		return locations;
	}

	public List<Location> getCubeLocations() {
		final ArrayList<Location> locations = new ArrayList<>();
		for (int x = xMin; x <= xMax; x++)
			for (int z = zMin; z <= zMax; z++) {
				locations.add(new Location(world, x, yMin, z));
				locations.add(new Location(world, x, yMax, z));
				if (z == zMin || x == xMin || z == zMax || x == xMax)
					for (int y = yMin + 1; y < yMax; y++)
						locations.add(new Location(world, x, y, z));
			}
		return locations;
	}

	@Override
	public List<Location> getLocations() {
		return Arrays.asList(min, max);
	}

	@Override
	public Location getRandomLocation() {
		final Random rand = ThreadLocalRandom.current();
		final int x = rand.nextInt(getXWidth()) + xMin;
		final int y = rand.nextInt(getHeight()) + yMin;
		final int z = rand.nextInt(getZWidth()) + zMin;
		return new Location(world, x, y, z);
	}

	public int getTotalBlockSize() {
		return getHeight() * getXWidth() * getZWidth();
	}

	public int getXWidth() {
		return xMax - xMin + 1;
	}

	public int getZWidth() {
		return zMax - zMin + 1;
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		return world == this.world && x >= xMin && x <= xMax && y >= yMin && y <= yMax && z >= zMin && z <= zMax;
	}

	public boolean isInWithMarge(final Location loc, final double marge) {
		return loc.getWorld() == world && loc.getX() >= xMinCentered - marge && loc.getX() <= xMaxCentered + marge && loc.getY() >= yMinCentered - marge && loc
				.getY() <= yMaxCentered + marge && loc.getZ() >= zMinCentered - marge && loc.getZ() <= zMaxCentered + marge;
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
