package fr.olympa.api.region.shapes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import fr.olympa.api.region.AbstractRegion;

public class Cylinder extends AbstractRegion {

	private final World world;
	private final int centerX, centerZ;
	private final int radius, radiusSquared;
	private final int minY, maxY;

	private Location min, max;
	private Random random = new Random();

	private List<Location> points;

	public Cylinder(Location center, int radius, int minY, int maxY) {
		this(center.getWorld(), center.getBlockX(), center.getBlockZ(), radius, minY, maxY);
	}

	public Cylinder(World world, int centerX, int centerZ, int radius, int minY, int maxY) {
		this.world = world;
		this.centerX = centerX;
		this.centerZ = centerZ;

		this.radius = radius;
		this.radiusSquared = radius * radius;
		this.minY = minY;
		this.maxY = maxY;

		min = new Location(world, centerX - radius, minY, centerZ - radius);
		max = new Location(world, centerX + radius, minY, centerZ + radius);

		points = Arrays.asList(new Location(world, centerX, minY, centerZ));
	}

	@Override
	public Location getMin() {
		return min;
	}

	@Override
	public Location getMax() {
		return max;
	}

	public int getCenterX() {
		return centerX;
	}

	public int getCenterZ() {
		return centerZ;
	}

	/**
	 * @return the radius
	 */
	public int getRadius() {
		return radius;
	}

	@Override
	public List<Location> getLocations() {
		return points;
	}

	@Override
	public Location getRandomLocation() {
		double r = getRadius() * Math.sqrt(random.nextDouble());
		double theta = random.nextDouble() * 2 * Math.PI;
		double x = centerX + r * Math.cos(theta);
		double z = centerZ + r * Math.sin(theta);
		double y = random.nextInt(maxY - minY) + minY;
		return new Location(world, x, y, z);
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		if (world != this.world) return false;
		if (y > maxY || y < minY) return false;
		int oX = Math.abs(x - centerX);
		int oZ = Math.abs(z - centerZ);
		if (oX > getRadius() || oZ > getRadius()) return false; // rapide si c'est en dehors du "carré"
		if (oX + oZ <= getRadius()) return true;
		return oX * oX + oZ * oZ <= radiusSquared;
	}

	@Override
	public World getWorld() {
		return world;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("world", world.getName());
		map.put("centerX", centerX);
		map.put("centerZ", centerZ);
		map.put("radius", getRadius());
		map.put("minY", minY);
		map.put("maxY", maxY);
		return map;
	}

	public static Cylinder deserialize(Map<String, Object> map) {
		return new Cylinder(Bukkit.getWorld((String) map.get("world")), (int) map.get("centerX"), (int) map.get("centerZ"), (int) map.get("radius"), (int) map.get("minY"), (int) map.get("maxY"));
	}

}
