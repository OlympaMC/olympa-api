package fr.olympa.api.region.tracking.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodFlag extends AbstractProtectionFlag {

	public FoodFlag(boolean protectedByDefault) {
		super(protectedByDefault);
	}

	public void foodEvent(FoodLevelChangeEvent event) {
		handleCancellable(event, (Player) event.getEntity());
	}

}
