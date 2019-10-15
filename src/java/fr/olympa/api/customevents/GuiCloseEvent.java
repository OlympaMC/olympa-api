package fr.olympa.api.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEvent;

public class GuiCloseEvent extends PlayerEvent {

	public static final HandlerList handlers = new HandlerList();
	private final InventoryCloseEvent inventoryCloseEvent;

	public GuiCloseEvent(final Player who, final InventoryCloseEvent inventoryCloseEvent) {
		super(who);
		this.inventoryCloseEvent = inventoryCloseEvent;
	}

	public InventoryCloseEvent getInventoryCloseEvent() {
		return this.inventoryCloseEvent;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public static HandlerList getHandlerList() {
		return handlers;
	}

}
