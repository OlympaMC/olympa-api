package fr.olympa.api.spigot.region.tracking.flags;

import java.util.StringJoiner;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;

import fr.olympa.api.spigot.region.tracking.BypassCommand;

public abstract class AbstractProtectionFlag extends Flag {

	protected boolean protectedByDefault;
	protected boolean overrideBypassProtection = false;

	public AbstractProtectionFlag(boolean protectedByDefault) {
		this.protectedByDefault = protectedByDefault;
	}
	
	protected void setBypassProtectionOverriden() {
		this.overrideBypassProtection = true;
	}

	public boolean isProtectedByDefault() {
		return protectedByDefault;
	}

	public void setProtectedByDefault(boolean protectedByDefault) {
		this.protectedByDefault = protectedByDefault;
	}
	
	protected boolean applies(Player player) {
		return overrideBypassProtection || player == null || !BypassCommand.bypasses.contains(player);
	}

	protected void handleCancellable(Cancellable event) {
		handleCancellable(event, null, protectedByDefault);
	}

	protected void handleCancellable(Cancellable event, Player player) {
		handleCancellable(event, player, protectedByDefault);
	}
	
	protected void handleCancellable(Cancellable event, Player player, boolean cancel) {
		if (applies(player)) event.setCancelled(cancel);
	}
	
	@Override
	public void appendDescription(StringJoiner joiner) {
		super.appendDescription(joiner);
		joiner.add("Protected by default: " + protectedByDefault);
		if (overrideBypassProtection) joiner.add("Overrides the BYPASS");
	}

}
