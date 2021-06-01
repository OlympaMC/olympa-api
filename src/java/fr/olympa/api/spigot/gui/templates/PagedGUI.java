package fr.olympa.api.spigot.gui.templates;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.observable.Observable;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;

/**
 * An inventory with an infinite amount of pages of 35 items (integer limit).
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the inventory
 */
public abstract class PagedGUI<T> extends OlympaGUI {

	public static ItemStack previousPage = ItemUtils.item(Material.ARROW, "§e↑ Page précédente");
	public static ItemStack nextPage = ItemUtils.item(Material.ARROW, "§e↓ Page suivante");

	private int page = 0;
	private int maxPage;
	
	protected List<T> objects;
	private int rows;
	private int itemsPerPage;
	
	private final String observableCode;
	
	protected PagedGUI(String name, DyeColor color, List<T> objects, int rows) {
		this(name, color, objects, rows, true);
	}
	
	protected PagedGUI(String name, DyeColor color, List<T> objects, int rows, boolean setItems) {
		super(name, rows);
		if (rows <= 1) throw new IllegalArgumentException("Rows must be higher than 1");
		this.rows = rows;
		this.itemsPerPage = 7 * rows;
		this.objects = objects;
		this.observableCode = name + hashCode();
		if (objects instanceof Observable) ((Observable) objects).observe(observableCode, this::itemChanged);
		calculateMaxPage();
		
		setBarItem(0, previousPage);
		setBarItem(rows - 1, nextPage);

		setSeparatorItems(color);
		
		if (setItems) setItems();
	}

	private void calculateMaxPage() {
		this.maxPage = objects.isEmpty() ? 1 : (int) Math.ceil(objects.size() * 1D / (double) itemsPerPage);
		if (page >= maxPage) page = maxPage - 1;
	}

	public void itemChanged() {
		calculateMaxPage();
		setItems();
	}

	protected void setItems() {
		for (int i = 0; i < itemsPerPage; i++) setMainItem(i, null);
		for (int i = page * itemsPerPage; i < objects.size(); i++) {
			if (i == (page + 1) * itemsPerPage) break;
			T obj = objects.get(i);
			setItem(i - page * itemsPerPage, obj);
		}
	}
	
	protected void setItem(int mainSlot, T object) {
		setMainItem(mainSlot, getItemStack(object));
	}
	
	protected int setMainItem(int mainSlot, ItemStack is) {
		int line = (int) Math.floor(mainSlot * 1.0 / 7.0);
		int slot = mainSlot + (2 * line);
		inv.setItem(slot, is);
		return slot;
	}
	
	protected void setSeparatorItems(DyeColor color) {
		for (int i = 0; i < rows; i++) inv.setItem(i * 9 + 7, ItemUtils.itemSeparator(color));
	}
	
	protected int setBarItem(int barSlot, ItemStack is) {
		int slot = barSlot * 9 + 8;
		inv.setItem(slot, is);
		return slot;
	}
	
	/**
	 * @param object T object to get the slot from
	 * @return slot in the inventory, -1 if the object is on another page
	 */
	public int getObjectSlot(T object){
		int index = objects.indexOf(object);
		if (index < page * itemsPerPage || index > (page + 1) * itemsPerPage) return -1;
		
		int line = (int) Math.floor(index * 1.0 / 7.0);
		return index + (2 * line);
	}

	public void updateObjectItem(T object, ItemStack item) {
		int slot = getObjectSlot(object);
		if (slot != -1) inv.setItem(slot, item);
	}

	public void removeItem(T existing) {
		if (!objects.remove(existing)) throw new IllegalArgumentException("Cannot remove nonexistent object.");
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		switch (slot % 9){
		case 8:
			int barSlot = (slot - 8) / 9;
			if (barSlot == 0) {
				if (page == 0) break;
				page--;
				setItems();
			}else if (barSlot == rows - 1) {
				if (page+1 == maxPage) break;
				page++;
				setItems();
			}else return onBarItemClick(p, current, barSlot, click);
			break;
			
		case 7:
			break;
			
		default:
			int line = (int) Math.floor(slot * 1D / 9D);
			int objectSlot = slot - line * 2 + page * itemsPerPage;
			click(objects.get(objectSlot), p, click);
			break;
		}
		return true;
	}
	
	@Override
	public boolean onClose(Player p) {
		if (objects instanceof Observable) ((Observable) objects).unobserve(observableCode);
		inv = null;
		return true;
	}

	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		return true;
	}

	/**
	 * @param object existing object to represent
	 * @return ItemStack who represents the object
	 */
	public abstract ItemStack getItemStack(T object);
	
	/**
	 * Called when an object is clicked
	 * @param existing clicked object
	 * @param p player which clicks
	 * @param click click type
	 */
	public abstract void click(T existing, Player p, ClickType click);

}
