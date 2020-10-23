package fr.olympa.api.region.tracking.flags;

import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public class PhysicsFlag extends AbstractProtectionFlag {

	public PhysicsFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	public <T extends Event & Cancellable> void blockEvent(T event, Block block) {
		handleCancellable(event);
	}

	public <T extends Event & Cancellable> void entityEvent(T event, Entity entity) {
		handleCancellable(event);
	}
	
}
