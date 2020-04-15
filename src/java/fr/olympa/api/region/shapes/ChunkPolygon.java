package fr.olympa.api.region.shapes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import fr.olympa.api.utils.Point2D;

public class ChunkPolygon extends Polygon {

	private Location minReal, maxReal;

	private ChunkPolygon(World world, List<Point2D> points) {
		super(world, points, 0, 256);
		minReal = new Location(world, super.getMin().getBlockX() * 16, 0, super.getMin().getBlockZ() * 16);
		maxReal = new Location(world, super.getMax().getBlockX() * 16, 256, super.getMax().getBlockZ() * 16);
	}

	@Override
	public Location getMin() {
		return minReal;
	}

	@Override
	public Location getMax() {
		return maxReal;
	}

	@Override
	public boolean isIn(Location loc) {
		return isIn(loc.getChunk());
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		return super.isIn(world, x / 16, y, z / 16);
	}

	@Override
	public boolean isIn(Chunk chunk) {
		return super.isIn(chunk.getWorld(), chunk.getX(), 0, chunk.getZ());
	}

	@Override
	protected Location pointToLocation(Point2D point) {
		return new Location(world, point.x * 16, minY, point.z * 16);
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();

		map.put("world", world.getName());
		map.put("points", points.stream().map(Point2D::toString).collect(Collectors.toList()));

		return map;
	}

	public static ChunkPolygon deserialize(Map<String, Object> map) {
		return new ChunkPolygon(
				Bukkit.getWorld((String) map.get("world")),
				((List<String>) map.get("points")).stream().map(x -> Point2D.fromString(x)).collect(Collectors.toList()));
	}

	public static ChunkPolygon create(World world, List<Chunk> chunks) {
		return new ChunkPolygon(world, chunks.stream().map(x -> new Point2D(x.getX(), x.getZ())).collect(Collectors.toList()));
	}

}
