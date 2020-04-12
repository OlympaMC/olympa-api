package fr.olympa.api.region;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.bukkit.block.Block;

public class RegionIterator implements Iterator<Block> {

	private Region region;
	private int nextX, nextY, nextZ;

	private boolean finished = false;

	public RegionIterator(Region region) {
		this.region = region;

		nextX = region.getMin().getBlockX();
		nextY = region.getMin().getBlockY();
		nextZ = region.getMin().getBlockZ();
	}

	@Override
	public boolean hasNext() {
		return !finished;
	}

	@Override
	public Block next() {
		if (!hasNext()) throw new NoSuchElementException();

		Block result = region.getWorld().getBlockAt(nextX, nextY, nextZ);

		forwardOne();
		forward();

		return result;
	}

	private void forwardOne() {
		if (++nextX <= region.getMax().getBlockX()) return;
		nextX = region.getMin().getBlockX();

		if (++nextZ <= region.getMax().getBlockZ()) return;
		nextZ = region.getMin().getBlockZ();

		if (++nextY <= region.getMax().getBlockY()) return;

		finished = true;
	}

	private void forward() {
		while (!finished && !region.isIn(region.getWorld(), nextX, nextY, nextZ)) {
			forwardOne();
		}
	}

}
