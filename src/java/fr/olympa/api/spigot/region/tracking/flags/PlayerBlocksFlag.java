package fr.olympa.api.spigot.region.tracking.flags;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PlayerBlocksFlag extends AbstractProtectionFlag {

	public PlayerBlocksFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	public <T extends Event & Cancellable> void blockEvent(T event, Player p, Block block) {
		handleCancellable(event, p);
	}

	public <T extends Event & Cancellable> void entityEvent(T event, Player p, Entity entity) {
		handleCancellable(event, p);
	}

}
