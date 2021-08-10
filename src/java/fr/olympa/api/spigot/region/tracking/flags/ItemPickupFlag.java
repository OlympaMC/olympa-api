package fr.olympa.api.spigot.region.tracking.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityPickupItemEvent;

public class ItemPickupFlag extends AbstractProtectionFlag {
	
	public ItemPickupFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}
	
	public void itemPickupEvent(EntityPickupItemEvent e) {
		handleCancellable(e, (Player) e.getEntity());
	}
	
}
