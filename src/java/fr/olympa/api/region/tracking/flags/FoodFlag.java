package fr.olympa.api.region.tracking.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodFlag extends AbstractProtectionFlag {

	private boolean disableUp;
	
	public FoodFlag(boolean protectedByDefault, boolean disableUp) {
		super(protectedByDefault);
		this.disableUp = disableUp;
	}

	public void foodEvent(FoodLevelChangeEvent event) {
		if (disableUp || ((Player) event.getEntity()).getFoodLevel() > event.getFoodLevel()) handleCancellable(event, (Player) event.getEntity());
	}

}
