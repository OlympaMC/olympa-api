package fr.olympa.api.region;

import java.util.Iterator;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public interface Region {

	Iterator<Block> blockList();

	Location getRandomLocation();

	int getTotalBlockSize();

	boolean isIn(Location loc);

	boolean isIn(Player player);

	boolean isInWithMarge(Location loc, double marge);

	World getWorld();

}