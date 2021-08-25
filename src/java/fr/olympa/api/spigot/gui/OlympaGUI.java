package fr.olympa.api.spigot.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.chat.ColorUtils;

public abstract class OlympaGUI implements InventoryHolder {

	protected Inventory inv;

	public OlympaGUI(String name, int rows) {
		inv = Bukkit.createInventory(this, 9 * rows, ColorUtils.color(name));
	}

	public OlympaGUI(int placeNeeded, String name) {
		inv = Bukkit.createInventory(this, (int) Math.ceil(placeNeeded / 9d) * 9, ColorUtils.color(name));
	}

	public OlympaGUI(String name, InventoryType type) {
		inv = Bukkit.createInventory(this, type,ColorUtils.color(name));
	}

	@Override
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
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return true;
	}

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
	 * Appelé lors d'un shift-click depuis l'inventaire du joueur
	 * @param p Joueur qui a bougé l'item
	 * @param moved Item déplacé
	 * @return true si le déplacement est annulé
	 */
	@Deprecated(forRemoval = true)
	public boolean onMoveItem(Player p, ItemStack moved) {
		return true;
	}

	/**
	 * Appelé lors d'un shift-click
	 * @param p Joueur qui a bougé l'item
	 * @param moved Item déplacé
	 * @param isFromPlayerInv Si l'item vient de l'inventaire du joueur
	 * @param i 
	 * @return true si le déplacement est annulé
	 */
	public boolean onMoveItem(Player p, ItemStack moved, boolean isFromPlayerInv, int slot) {
		if (isFromPlayerInv) {
			return onMoveItem(p, moved);
		}
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

	public boolean noDoubleClick() {
		return true;
	}
	
	public boolean noNumberKey() {
		return true;
	}

	public boolean noMiddleClick() {
		return true;
	}

	public boolean noRightClick() {
		return false;
	}
	
	public boolean noLeftClick() {
		return false;
	}
	
	public boolean noDropClick() {
		return false;
	}

	public boolean noDragClick() {
		return true;
	}
	
	/**
	 * Opens the inventory to the player. Direct reference to {@link Inventories#create(Player, OlympaGUI)}
	 * @param p Player
	 * @see Inventories#create(Player, OlympaGUI)
	 */
	public final void create(Player p) {
		Inventories.create(p, this);
	}



}
