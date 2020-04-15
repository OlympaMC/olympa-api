package fr.olympa.api.region;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;

public interface Region extends ConfigurationSerializable {

	Location getMin();

	Location getMax();

	Location getRandomLocation();

	Iterator<Block> blockList();

	List<Location> getLocations();

	boolean isIn(World world, int x, int y, int z);

	boolean isIn(Location loc);

	boolean isIn(Player player);

	boolean isIn(Chunk chunk);

	World getWorld();

	Predicate<Player> getEnterPredicate();

	Predicate<Player> getExitPredicate();

	void setExitPredicate(Predicate<Player> exitPredicate);

	void setEnterPredicate(Predicate<Player> enterPredicate);

}