package fr.olympa.api.spigot.region.tracking.flags;

import org.bukkit.block.Block;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockRedstoneEvent;

public class RedstoneFlag extends AbstractProtectionFlag {
	
	public RedstoneFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}
	
	public <T extends Event & Cancellable> void blockEvent(T event, Block block) {
		handleCancellable(event);
	}
	
	public void redstoneEvent(BlockRedstoneEvent event) {
		if (protectedByDefault) event.setNewCurrent(event.getOldCurrent());
	}
	
}
