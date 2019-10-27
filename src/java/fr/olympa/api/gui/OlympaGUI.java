package fr.olympa.api.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.old.OlympaGui;
import fr.olympa.api.utils.SpigotUtils;

public abstract class OlympaGUI implements InventoryHolder {
	
	protected Inventory inv;

	public OlympaGUI(String name, int rows) {
		this.inv = Bukkit.createInventory(this, 9 * rows, SpigotUtils.color(name));
	}

	public OlympaGUI(String name, InventoryType type) {
		this.inv = Bukkit.createInventory(this, type, SpigotUtils.color(name));
	}

	public Inventory getInventory() {
		return inv;
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
	public abstract boolean onClick(Player p, ItemStack current, int slot, ClickType click);
	
	/**
	 * Called when clicking on an item <b>with something on the cursor</b>
	 * @param p Player who clicked
	 * @param current Item clicked
	 * @param cursor Item on the cursor when click
	 * @param slot Slot of item clicked
	 * @return Cancel click
	 */
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return true;
	}
	
	/**
	 * Called when closing the inventory
	 * @param p Player who has the inventory opened
	 * @return Remove player from inventories system
	 */
	public boolean onClose(Player p) {
		return true;
	}

	/**
	 * Opens the inventory to the player. Direct reference to {@link Inventories#create(Player, CustomInventory, OlympaGui)}
	 * @param p Player
	 * @see Inventories#create(Player, CustomInventory)
	 */
	public final void create(Player p) {
		Inventories.create(p, this);
	}
	
}
