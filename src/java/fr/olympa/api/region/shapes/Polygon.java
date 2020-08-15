package fr.olympa.api.region.shapes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

import com.google.common.collect.ImmutableList;

import fr.olympa.api.region.AbstractRegion;
import fr.olympa.api.utils.Point2D;

public class Polygon extends AbstractRegion {

	protected final ImmutableList<Point2D> points;
	protected final int minY, maxY;
	protected final Location min, max;
	protected World world;

	public Polygon(World world, List<Point2D> points, int minY, int maxY) {
		this.world = world;
		this.points = ImmutableList.copyOf(points);

		int minX = points.get(0).x;
		int minZ = points.get(0).z;
		int maxX = minX;
		int maxZ = minZ;

		for (Point2D v : points) {
			if (v.x < minX) minX = v.x;
			if (v.z < minZ) minZ = v.z;

			if (v.x > maxX) maxX = v.x;
			if (v.z > maxZ) maxZ = v.z;
		}

		this.min = new Location(world, minX, minY, minZ);
		this.max = new Location(world, maxX, maxY, maxZ);
		this.minY = minY;
		this.maxY = maxY;
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
	public Location getRandomLocation() {
		Random ran = new Random();
		int x, z;
		int y = ran.nextInt(maxY - minY) + minY;
		int xDistance = max.getBlockX() - min.getBlockX();
		int zDistance = max.getBlockZ() - min.getBlockZ();
		do {
			x = ran.nextInt(xDistance) + min.getBlockX();
			z = ran.nextInt(zDistance) + min.getBlockZ();
		}while (!isIn(world, x, y, z));
		return new Location(world, x, y, z);
	}

	@Override
	public List<Location> getLocations() {
		return points.stream().map(this::pointToLocation).collect(Collectors.toList());
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		if (world != this.world) return false;
		if (y < minY || y > maxY) return false;
		
		boolean inside = false;
		int npoints = points.size();
		int xNew, zNew;
		int xOld, zOld;
		int x1, z1;
		int x2, z2;
		long crossproduct;
		int i;
		
		xOld = points.get(npoints - 1).getX();
		zOld = points.get(npoints - 1).getZ();
		
		for (i = 0; i < npoints; i++) {
			xNew = points.get(i).getX();
			zNew = points.get(i).getZ();
			//Check for corner
			if (xNew == x && zNew == z) {
				return true;
			}
			if (xNew > xOld) {
				x1 = xOld;
				x2 = xNew;
				z1 = zOld;
				z2 = zNew;
			}else {
				x1 = xNew;
				x2 = xOld;
				z1 = zNew;
				z2 = zOld;
			}
			if (x1 <= x && z <= x2) {
				crossproduct = ((long) z - (long) z1) * (long) (x2 - x1) - ((long) z2 - (long) z1) * (long) (x - x1);
				if (crossproduct == 0) {
					if ((z1 <= z) == (z <= z2)) return true; // on edge
				}else if (crossproduct < 0 && (x1 != x)) {
					inside = !inside;
				}
			}
			xOld = xNew;
			zOld = zNew;
		}
		
		return inside;
		
		/*if (x < min.getBlockX() || x > max.getBlockX() || z < min.getBlockZ() || z > max.getBlockZ()) return false;
		
		int i, j;
		boolean result = false;
		for (i = 0, j = points.size() - 1; i < points.size(); j = i++) {
			if ((points.get(i).z > z) != (points.get(j).z > z) &&
					(x < (points.get(j).x - points.get(i).x) * (z - points.get(i).z) / (points.get(j).z - points.get(i).z) + points.get(i).x)) {
				result = !result;
			}
		}
		return result;*/
	}

	@Override
	public World getWorld() {
		return world;
	}

	protected Location pointToLocation(Point2D point) {
		return new Location(world, point.x, minY, point.z);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();

		map.put("world", world.getName());
		map.put("minY", minY);
		map.put("maxY", maxY);
		map.put("points", points.stream().map(Point2D::toString).collect(Collectors.toList()));

		return map;
	}

	public static Polygon deserialize(Map<String, Object> map) {
		return new Polygon(
				Bukkit.getWorld((String) map.get("world")),
				((List<String>) map.get("points")).stream().map(x -> Point2D.fromString(x)).collect(Collectors.toList()),
				(int) map.get("minY"),
				(int) map.get("maxY"));
	}

}
