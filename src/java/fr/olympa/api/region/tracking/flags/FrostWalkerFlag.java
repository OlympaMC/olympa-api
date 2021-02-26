package fr.olympa.api.region.tracking.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.EntityBlockFormEvent;

public class FrostWalkerFlag extends AbstractProtectionFlag {
	
	public FrostWalkerFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}
	
	public void formEvent(EntityBlockFormEvent e) {
		handleCancellable(e, (Player) e.getEntity());
	}
	
	public void meltEvent(BlockFadeEvent e) {
		handleCancellable(e);
	}
	
}
