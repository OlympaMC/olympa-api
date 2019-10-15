package fr.olympa.api.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public abstract interface CustomInventory {
	
	/**
	 * Called when opening inventory
	 * @param p Player to open
	 * @return inventory opened
	 */
	public abstract Inventory open(Player p);
	
	/**
	 * Called when clicking on an item
	 * @param p Player who clicked
	 * @param inv Inventory clicked
	 * @param current Item clicked
	 * @param slot Slot of item clicked
	 * @param click Type of click
	 * @return Cancel click
	 */
	public abstract boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click);
	
	/**
	 * Called when clicking on an item <b>with something on the cursor</b>
	 * @param p Player who clicked
	 * @param inv Inventory clicked
	 * @param current Item clicked
	 * @param cursor Item on the cursor when click
	 * @param slot Slot of item clicked
	 * @return Cancel click
	 */
	public default boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot){return true;}
	
	/**
	 * Called when closing the inventory
	 * @param p Player who has the inventory opened
	 * @param inv Inventory closed
	 * @return Remove player from inventories system
	 */
	public default boolean onClose(Player p, Inventory inv){
		return true;
	}

	/**
	 * Opens the inventory to the player. Direct reference to {@link Inventories#create(Player, CustomInventory)}
	 * @param p Player
	 * @see Inventories#create(Player, CustomInventory)
	 */
	public default void create(Player p){
		Inventories.create(p, this);
	}
	
}
