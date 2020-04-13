package fr.olympa.api.region.shapes;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

import fr.olympa.api.region.ChunkRegion;

public class ChunkCuboid extends ExpandedCuboid implements ChunkRegion {

	private int chunkXMin;
	private int chunkZMin;
	private int chunkXMax;
	private int chunkZMax;

	public ChunkCuboid(World world, int xMin, int zMin, int xMax, int zMax) {
		super(world, xMin * 16, zMin * 16, (xMax + 1) * 16 - 1, (zMax + 1) * 16 - 1);
		this.chunkXMin = xMin;
		this.chunkZMin = zMin;
		this.chunkXMax = xMax;
		this.chunkZMax = zMax;
	}

	public boolean isIn(Chunk chunk) {
		return chunk.getWorld() == super.world && chunk.getX() >= chunkXMin && chunk.getX() <= chunkXMax && chunk.getZ() >= chunkZMin && chunk.getZ() <= chunkZMax;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("world", world.getName());
		map.put("chunkXMin", chunkXMin);
		map.put("chunkXMax", chunkXMax);
		map.put("chunkZMin", chunkZMin);
		map.put("chunkZMax", chunkZMax);
		return map;
	}

	public static ExpandedCuboid deserialize(Map<String, Object> map) {
		return new ExpandedCuboid(Bukkit.getWorld((String) map.get("world")), (int) map.get("chunkXMin"), (int) map.get("chunkZMin"), (int) map.get("chunkXMax"), (int) map.get("chunkZMax"));
	}

	public static ChunkCuboid create(Chunk c1, Chunk c2) {
		Validate.isTrue(c1.getWorld() == c2.getWorld(), "Chunks must be in the same world");
		int xMin = Math.min(c1.getX(), c2.getX());
		int xMax = Math.max(c1.getX(), c2.getX());
		int zMin = Math.min(c1.getZ(), c2.getZ());
		int zMax = Math.max(c1.getZ(), c2.getZ());
		return new ChunkCuboid(c1.getWorld(), xMin, zMin, xMax, zMax);
	}

}
