package fr.olympa.api.region.tracking.flags;

import java.lang.reflect.Method;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerEvent;

public abstract class AbstractProtectionFlag extends Flag {

	protected boolean protectedByDefault;

	public AbstractProtectionFlag(boolean protectedByDefault) {
		this.setProtectedByDefault(protectedByDefault);
	}

	public boolean isProtectedByDefault() {
		return protectedByDefault;
	}

	public void setProtectedByDefault(boolean protectedByDefault) {
		this.protectedByDefault = protectedByDefault;
	}
	
	@Override
	public void onEvent(Event event) {
		System.out.println("AbstractProtectionFlag.onEvent()");
		Cancellable cancellable = (Cancellable) event;
		boolean cancel = protectedByDefault;
		if (event instanceof PlayerEvent) {
			cancel = playerEvent(event, ((PlayerEvent) event).getPlayer());
		}else {
			try {
				Method getPlayer = event.getClass().getDeclaredMethod("getPlayer");
				cancel = playerEvent(event, (Player) getPlayer.invoke(event));
			}catch (NoSuchMethodException ex) {
				cancel = normalEvent(event);
			}catch (ReflectiveOperationException ex) {
				ex.printStackTrace();
			}
		}
		cancellable.setCancelled(cancel);
	}

	protected boolean normalEvent(Event event) {
		return protectedByDefault;
	}

	protected boolean playerEvent(Event event, Player p) {
		return protectedByDefault;
	}

}
