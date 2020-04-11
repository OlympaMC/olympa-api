package fr.olympa.api.region;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkCuboid extends ExpandedCuboid{

	private int xMaxExcluded, zMaxExcluded;
	
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
		this.xMaxExcluded = super.xMax + 16;
		this.zMaxExcluded = super.zMax + 16;
	}
	
	public boolean isIn(Chunk chunk) {
		return chunk.getWorld() == super.world && chunk.getX() >= super.xMin && chunk.getX() < xMaxExcluded && chunk.getZ() >= super.zMin && chunk.getZ() < zMaxExcluded;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("world", world.getName());
		map.put("chunkXMin", xMin);
		map.put("chunkXMax", xMax);
		map.put("chunkZMin", zMin);
		map.put("chunkZMax", zMax);
		return map;
	}

	public static ExpandedCuboid deserialize(Map<String, Object> map) {
		return new ExpandedCuboid(Bukkit.getWorld((String) map.get("world")), (int) map.get("chunkXMin"), (int) map.get("chunkZMin"), (int) map.get("chunkXMax"), (int) map.get("chunkZMax"));
	}

}
