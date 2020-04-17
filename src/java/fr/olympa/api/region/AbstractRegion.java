package fr.olympa.api.region;

import java.util.Iterator;
import java.util.function.Predicate;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public abstract class AbstractRegion implements Region {

	private static final Predicate<Player> FALSE_PREDICATE = x -> false;

	private Predicate<Player> enterPredicate;
	private Predicate<Player> exitPredicate;

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
	public boolean isIn(Chunk chunk) {
		return isIn(chunk.getWorld(), chunk.getX() * 16, getMin().getBlockY(), chunk.getZ() * 16);
	}

	@Override
	public abstract World getWorld();

	@Override
	public Predicate<Player> getEnterPredicate() {
		return enterPredicate == null ? FALSE_PREDICATE : enterPredicate;
	}

	@Override
	public Predicate<Player> getExitPredicate() {
		return exitPredicate == null ? FALSE_PREDICATE : exitPredicate;
	}

	@Override
	public void setEnterPredicate(Predicate<Player> enterPredicate) {
		this.enterPredicate = enterPredicate;
	}

	@Override
	public void setExitPredicate(Predicate<Player> exitPredicate) {
		this.exitPredicate = exitPredicate;
	}

}