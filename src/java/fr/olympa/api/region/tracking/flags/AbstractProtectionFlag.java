package fr.olympa.api.region.tracking.flags;

import java.util.Optional;

import org.bukkit.event.Cancellable;

public abstract class AbstractProtectionFlag extends Flag {

	protected boolean protectedByDefault;

	public AbstractProtectionFlag(boolean protectedByDefault) {
		this.protectedByDefault = protectedByDefault;
	}

	public boolean isProtectedByDefault() {
		return protectedByDefault;
	}

	public void setProtectedByDefault(boolean protectedByDefault) {
		this.protectedByDefault = protectedByDefault;
	}

	protected void handleCancellable(Cancellable event) {
		handleCancellable(event, protectedByDefault);
	}

	protected void handleCancellable(Cancellable event, Optional<Boolean> cancel) {
		handleCancellable(event, cancel.orElse(protectedByDefault));
	}

	protected void handleCancellable(Cancellable event, boolean cancel) {
		event.setCancelled(cancel);
	}

}
