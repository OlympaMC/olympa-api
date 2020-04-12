package fr.olympa.api.region;

import org.bukkit.Chunk;

public interface ChunkRegion extends Region {

	boolean isIn(Chunk chunk);

}
