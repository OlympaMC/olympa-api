package fr.olympa.api.region.tracking.flags;

import org.bukkit.event.player.PlayerDropItemEvent;

public class DropFlag extends AbstractProtectionFlag {

	public DropFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	public void dropEvent(PlayerDropItemEvent event) {
		handleCancellable(event);
	}

}
