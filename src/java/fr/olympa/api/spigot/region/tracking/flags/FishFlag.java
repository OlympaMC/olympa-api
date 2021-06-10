package fr.olympa.api.spigot.region.tracking.flags;

import org.bukkit.event.player.PlayerFishEvent;

public class FishFlag extends AbstractProtectionFlag {
	
	public FishFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}
	
	public void fishEvent(PlayerFishEvent event) {
		handleCancellable(event);
	}
	
}
