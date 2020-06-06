package fr.olympa.api.region.tracking.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.block.BlockEvent;

public class PlayerBlocksFlag extends AbstractProtectionFlag {

	public PlayerBlocksFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	@Override
	protected boolean playerEvent(Event event, Player p) {
		return blockEvent((BlockEvent) event, p);
	}

	public boolean blockEvent(BlockEvent event, Player p) {
		System.out.println("PlayerBlocksFlag.blockEvent()");
		return protectedByDefault;
	}

}
