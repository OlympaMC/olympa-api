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
			x = ran.nextInt(xDistance) + max.getBlockX();
			z = ran.nextInt(zDistance) + max.getBlockZ();
		}while (!isIn(world, x, y, z));
		return new Location(world, x, y, z);
	}

	@Override
	public List<Point2D> getCorners() {
		return points;
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		if (y < minY || y > maxY) return false;
		if (x < min.getBlockX() || x > max.getBlockX() || z < min.getBlockZ() || z > max.getBlockZ()) return false;

		return isInside(points, max.getBlockX(), new Point2D(x, z));
	}

	@Override
	public World getWorld() {
		return world;
	}

	// Given three colinear points p, q, r,  
	// the function checks if point q lies 
	// on line segment 'pr' 
	public static boolean onSegment(Point2D p, Point2D q, Point2D r) {
		if (q.x <= Math.max(p.x, r.x) &&
				q.x >= Math.min(p.x, r.x) &&
				q.z <= Math.max(p.z, r.z) &&
				q.z >= Math.min(p.z, r.z)) {
			return true;
		}
		return false;
	}

	// To find orientation of ordered triplet (p, q, r). 
	// The function returns following values 
	// 0 --> p, q and r are colinear 
	// 1 --> Clockwise 
	// 2 --> Counterclockwise 
	public static int orientation(Point2D p, Point2D q, Point2D r) {
		int val = (q.z - p.z) * (r.x - q.x)
				- (q.x - p.x) * (r.z - q.z);

		if (val == 0) {
			return 0; // colinear 
		}
		return (val > 0) ? 1 : 2; // clock or counterclock wise 
	}

	// The function that returns true if  
	// line segment 'p1q1' and 'p2q2' intersect. 
	public static boolean doIntersect(Point2D p1, Point2D q1, Point2D p2, Point2D q2) {
		// Find the four orientations needed for  
		// general and special cases 
		int o1 = orientation(p1, q1, p2);
		int o2 = orientation(p1, q1, q2);
		int o3 = orientation(p2, q2, p1);
		int o4 = orientation(p2, q2, q1);

		// General case 
		if (o1 != o2 && o3 != o4) {
			return true;
		}

		// Special Cases 
		// p1, q1 and p2 are colinear and 
		// p2 lies on segment p1q1 
		if (o1 == 0 && onSegment(p1, p2, q1)) {
			return true;
		}

		// p1, q1 and p2 are colinear and 
		// q2 lies on segment p1q1 
		if (o2 == 0 && onSegment(p1, q2, q1)) {
			return true;
		}

		// p2, q2 and p1 are colinear and 
		// p1 lies on segment p2q2 
		if (o3 == 0 && onSegment(p2, p1, q2)) {
			return true;
		}

		// p2, q2 and q1 are colinear and 
		// q1 lies on segment p2q2 
		if (o4 == 0 && onSegment(p2, q1, q2)) {
			return true;
		}

		// Doesn't fall in any of the above cases 
		return false;
	}

	public static boolean isInside(List<Point2D> points, int maxX, Point2D point) {
		// Create a point for line segment from p to infinite 
		Point2D extreme = new Point2D(maxX + 1, point.z);

		// Count intersections of the above line  
		// with sides of polygon 
		int count = 0, i = 0;
		do {
			int next = (i + 1) % points.size();

			// Check if the line segment from 'p' to  
			// 'extreme' intersects with the line  
			// segment from 'polygon[i]' to 'polygon[next]' 
			if (doIntersect(points.get(i), points.get(next), point, extreme)) {
				// If the point 'p' is colinear with line  
				// segment 'i-next', then check if it lies  
				// on segment. If it lies, return true, otherwise false 
				if (orientation(points.get(i), point, points.get(next)) == 0) {
					return onSegment(points.get(i), point,
							points.get(next));
				}

				count++;
			}
			i = next;
		}while (i != 0);

		// Return true if count is odd, false otherwise 
		return (count % 2 == 1);
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
