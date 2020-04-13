package fr.olympa.api.region;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Region {

	Location getMin();

	Location getMax();

	Location getRandomLocation();

	default Iterator<Block> blockList() {
		return new RegionIterator(this);
	}

	boolean isIn(World world, int x, int y, int z);

	default boolean isIn(Location loc) {
		return isIn(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	default boolean isIn(Player player) {
		return isIn(player.getLocation());
	}

	World getWorld();

}