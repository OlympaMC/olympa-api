package fr.olympa.api.spigot.region;

import org.bukkit.Chunk;

public interface ChunkRegion extends Region {

	boolean isIn(Chunk chunk);

}
