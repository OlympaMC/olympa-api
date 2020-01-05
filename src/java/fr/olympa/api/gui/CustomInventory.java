package fr.olympa.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.old.OlympaGui;

@Deprecated
public abstract interface CustomInventory {

	/**
	 * Called when opening inventory
	 * @param p Player to open
	 * @return inventory opened

	public abstract OlympaGui open(Player p);*/

	/**
	 * Opens the inventory to the player. Direct reference to {@link Inventories#create(Player, CustomInventory, OlympaGui)}
	 * @param p Player
	 * @param gui OlympaGui
	 * @see Inventories#create(Player, CustomInventory)
	 */
	default void create(Player p, OlympaGui gui) {
		// Inventories.create(p, this, gui);
	}

	/**
	 * Called when clicking on an item
	 * @param p Player who clicked
	 * @param inv Inventory clicked
	 * @param current Item clicked
	 * @param slot Slot of item clicked
	 * @param click Type of click
	 * @return Cancel click
	 */
	boolean onClick(Player p, OlympaGui inv, ItemStack current, int slot, ClickType click);

	/**
	 * Called when clicking on an item <b>with something on the cursor</b>
	 * @param p Player who clicked
	 * @param inv Inventory clicked
	 * @param current Item clicked
	 * @param cursor Item on the cursor when click
	 * @param slot Slot of item clicked
	 * @return Cancel click
	 */
	default boolean onClickCursor(Player p, OlympaGui inv, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}

	/**
	 * Called when closing the inventory
	 * @param p Player who has the inventory opened
	 * @param inv Inventory closed
	 * @return Remove player from inventories system
	 */
	default boolean onClose(Player p, OlympaGui inv) {
		return true;
	}

}
