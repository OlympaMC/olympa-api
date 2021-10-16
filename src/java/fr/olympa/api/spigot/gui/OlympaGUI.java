package fr.olympa.api.spigot.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.chat.ColorUtils;

public abstract class OlympaGUI extends GUIView implements InventoryHolder, GUIChanger {

	protected Inventory inv;
	private int rows;

	public OlympaGUI(String name, int rows) {
		this(rows);
		inv = Bukkit.createInventory(this, 9 * getRows(), ColorUtils.color(name));
	}

	public OlympaGUI(int placeNeeded, String name) {
		this(placeNeeded <= 0 ? 9 : (int) Math.ceil(placeNeeded / 9d));
		inv = Bukkit.createInventory(this, getRows() * 9, ColorUtils.color(name));
	}

	public OlympaGUI(String name, InventoryType type) {
		this(type.getDefaultSize() % 9 == 0 ? type.getDefaultSize() / 9 : -1);
		inv = Bukkit.createInventory(this, type, ColorUtils.color(name));
	}
	
	private OlympaGUI(int rows) {
		this.rows = rows;
		setGUI(this);
	}

	@Override
	public Inventory getInventory() {
		return inv;
	}
	
	@Override
	public void setItem(int slot, ItemStack item) {
		inv.setItem(slot, item);
	}
	
	@Override
	public ItemStack getItem(int slot) {
		return inv.getItem(slot);
	}
	
	@Override
	public void clear() {
		inv.clear();
	}
	
	@Override
	public int getRows() {
		return rows;
	}
	
	@Override
	public int getColumns() {
		if (rows == -1) throw new IllegalArgumentException("This GUI is not a chest GUI");
		return 9;
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
	@Override
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
	@Override
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
