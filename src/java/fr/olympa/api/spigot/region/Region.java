package fr.olympa.api.spigot.region;

import java.util.Iterator;
import java.util.List;

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

	World getWorld();

}