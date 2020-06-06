package fr.olympa.api.region.shapes;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldRegion extends ExpandedCuboid {

	public WorldRegion(World world) {
		super(world, -30_000_000, -30_000_000, 30_000_000, 30_000_000);
	}

	@Override
	public boolean isIn(World world, int x, int y, int z) {
		return this.world.equals(world);
	}

	@Override
	public boolean isIn(Location loc) {
		return this.world.equals(loc.getWorld());
	}

	@Override
	public boolean isIn(Player player) {
		return this.world.equals(player.getWorld());
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("world", world.getName());
		return map;
	}

	public static WorldRegion deserialize(Map<String, Object> map) {
		return new WorldRegion(Bukkit.getWorld((String) map.get("world")));
	}

}
