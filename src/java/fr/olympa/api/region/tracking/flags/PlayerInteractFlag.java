package fr.olympa.api.region.tracking.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerInteractFlag extends AbstractProtectionFlag {

	public PlayerInteractFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	@Override
	protected boolean playerEvent(Event event, Player p) {
		return interactEvent((PlayerInteractEvent) event, p);
	}

	public boolean interactEvent(PlayerInteractEvent event, Player p) {
		return protectedByDefault;
	}

}
