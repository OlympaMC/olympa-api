package fr.olympa.api.spigot.region.tracking.flags;

import java.util.StringJoiner;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class FoodFlag extends AbstractProtectionFlag {

	private boolean handleUp, disableUp;
	
	public FoodFlag(boolean protectedByDefault) {
		this(protectedByDefault, false, false);
	}
	
	public FoodFlag(boolean protectedByDefault, boolean handleUp, boolean disableUp) {
		super(protectedByDefault);
		this.handleUp = handleUp;
		this.disableUp = disableUp;
	}

	public void foodEvent(FoodLevelChangeEvent event) {
		if (handleUp) {
			handleCancellable(event, (Player) event.getEntity(), event.getEntity().getFoodLevel() < event.getFoodLevel() ? disableUp : protectedByDefault);
		}else handleCancellable(event, (Player) event.getEntity());
	}
	
	@Override
	public void appendDescription(StringJoiner joiner) {
		super.appendDescription(joiner);
		if (handleUp && disableUp) joiner.add(" Disables up");
	}

}
