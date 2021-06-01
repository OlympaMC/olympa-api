package fr.olympa.api.spigot.region;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class AbstractRegion implements Region {

	@Override
	public abstract Location getMin();

	@Override
	public abstract Location getMax();

	@Override
	public abstract Location getRandomLocation();

	@Override
	public Iterator<Block> blockList() {
		return new RegionIterator(this);
	}

	@Override
	public abstract boolean isIn(World world, int x, int y, int z);

	@Override
	public boolean isIn(Location loc) {
		return isIn(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	@Override
	public boolean isIn(Player player) {
		return isIn(player.getLocation());
	}

	@Override
	public abstract World getWorld();

}