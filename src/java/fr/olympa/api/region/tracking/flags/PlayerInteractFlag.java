package fr.olympa.api.region.tracking.flags;

import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractFlag extends AbstractProtectionFlag {

	public PlayerInteractFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	public void interactEvent(PlayerInteractEvent event) {
		handleCancellable(event);
	}

}
