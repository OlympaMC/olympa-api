package fr.olympa.api.region;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

public class ChunkPolygon extends Polygon implements ChunkRegion {

	private Location minReal, maxReal;

	public ChunkPolygon(World world, List<Chunk> points) {
		super(world, points.stream().map(x -> new Point2D(x.getX(), x.getZ())).collect(Collectors.toList()), 0, 256);
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

}
