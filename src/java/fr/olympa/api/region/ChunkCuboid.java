package fr.olympa.api.region;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkCuboid extends ExpandedCuboid implements ChunkRegion {

	private int chunkXMin;
	private int chunkZMin;
	private int chunkXMax;
	private int chunkZMax;
	
	/**
	 * Region containing chunks from (xMin, zMin) to (xMax, zMax), chunk coordinates, included.
	 * @param world
	 * @param x1
	 * @param z1
	 * @param x2
	 * @param z2
	 */
	public ChunkCuboid(World world, int x1, int z1, int x2, int z2) {
		super(world, x1*16, z1*16, x2*16, z2*16);
		this.chunkXMin = super.xMin / 16;
		this.chunkZMin = super.zMin / 16;
		this.chunkXMax = super.xMax / 16;
		this.chunkZMax = super.zMax / 16;
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

}
