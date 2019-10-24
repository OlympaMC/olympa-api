package fr.olympa.api.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerEvent;

import fr.olympa.api.gui.OlympaGui;

public class GuiClickEvent extends PlayerEvent {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	private final InventoryClickEvent inventoryClickEvent;
	private final OlympaGui gui;

	public GuiClickEvent(Player who, InventoryClickEvent inventoryClickEvent, OlympaGui gui) {
		super(who);
		this.inventoryClickEvent = inventoryClickEvent;
		this.gui = gui;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public InventoryClickEvent getInventoryClickEvent() {
		return this.inventoryClickEvent;
	}

	public OlympaGui getGui() {
		return gui;
	}

}
