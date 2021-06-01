package fr.olympa.api.spigot.region.tracking.flags;

import org.bukkit.event.player.PlayerItemDamageEvent;

public class ItemDurabilityFlag extends AbstractProtectionFlag {

	public ItemDurabilityFlag(boolean protectedByDefault) {
		super(protectedByDefault);
		setBypassProtectionOverriden();
	}

	public void itemDamageEvent(PlayerItemDamageEvent event) {
		handleCancellable(event);
	}

}
