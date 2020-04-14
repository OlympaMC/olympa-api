package fr.olympa.api.region;

import java.util.Iterator;
import java.util.function.Predicate;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public abstract class Region implements ConfigurationSerializable {

	private static final Predicate<Player> FALSE_PREDICATE = x -> false;

	private Predicate<Player> enterPredicate;
	private Predicate<Player> exitPredicate;

	public abstract Location getMin();

	public abstract Location getMax();

	public abstract Location getRandomLocation();

	public Iterator<Block> blockList() {
		return new RegionIterator(this);
	}

	public abstract boolean isIn(World world, int x, int y, int z);

	public boolean isIn(Location loc) {
		return isIn(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
	}

	public boolean isIn(Player player) {
		return isIn(player.getLocation());
	}

	public abstract World getWorld();

	public Predicate<Player> getEnterPredicate() {
		return enterPredicate == null ? FALSE_PREDICATE : enterPredicate;
	}

	public void setEnterPredicate(Predicate<Player> enterPredicate) {
		this.enterPredicate = enterPredicate;
	}

	public Predicate<Player> getExitPredicate() {
		return exitPredicate == null ? FALSE_PREDICATE : exitPredicate;
	}

	public void setExitPredicate(Predicate<Player> exitPredicate) {
		this.exitPredicate = exitPredicate;
	}

}