package fr.olympa.api.region.tracking.flags;

import org.bukkit.event.player.PlayerItemDamageEvent;

public class ItemDurabilityFlag extends AbstractProtectionFlag {

	public ItemDurabilityFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	public void itemDamageEvent(PlayerItemDamageEvent event) {
		handleCancellable(event);
	}

}
