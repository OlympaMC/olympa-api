package fr.olympa.api.region.shapes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.util.NumberConversions;

public class ExpandedCuboid extends Cuboid {

	public ExpandedCuboid(World world, int x1, int z1, int x2, int z2) {
		super(world, x1, 0, z1, x2, 256, z2);
	}

	@Override
	public int getHeight() {
		return 256;
	}

	@Override
	public double getDistanceSquared() {
		return NumberConversions.square(xMax - xMin) + NumberConversions.square(zMax - zMin);
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		return world == this.world && x >= this.xMin && x <= this.xMax && z >= this.zMin && z <= this.zMax;
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("world", world.getName());
		map.put("xMin", xMin);
		map.put("xMax", xMax);
		map.put("zMin", zMin);
		map.put("zMax", zMax);
		return map;
	}

	public static ExpandedCuboid deserialize(Map<String, Object> map) {
		return new ExpandedCuboid(Bukkit.getWorld((String) map.get("world")), (int) map.get("xMin"), (int) map.get("zMin"), (int) map.get("xMax"), (int) map.get("zMax"));
	}

}
