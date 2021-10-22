package fr.olympa.api.spigot.gui.templates;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.observable.Observable;
import fr.olympa.api.spigot.gui.GUIView;
import fr.olympa.api.spigot.item.ItemUtils;

/**
 * An inventory with an infinite amount of pages of 35 items (integer limit).
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the inventory
 */
public abstract class PagedView<T> extends HSplitView {

	private static final int BAR_RIGHT_OFFSET = 2;
	
	public static ItemStack previousPage = ItemUtils.item(Material.ARROW, "§e↑ Page précédente");
	public static ItemStack nextPage = ItemUtils.item(Material.ARROW, "§e↓ Page suivante");

	private int page = 0;
	private int maxPage;
	
	protected List<T> objects;
	
	private final String observableCode;
	
	protected PagedView(DyeColor color, List<T> objects) {
		super(color, BAR_RIGHT_OFFSET);
		this.objects = objects;
		this.observableCode = color.toString() + hashCode();
		if (objects instanceof Observable observable) observable.observe(observableCode, this::itemChanged);
	}

	@Override
	public void init() {
		super.init();
		setViews(new Left(), new Right());
		calculateMaxPage();
		
		setItems();
	}
	
	private void calculateMaxPage() {
		this.maxPage = objects.isEmpty() ? 1 : (int) Math.ceil(objects.size() * 1D / leftItems);
		if (page >= maxPage) page = maxPage - 1;
	}

	public void itemChanged() {
		calculateMaxPage();
		setItems();
	}

	protected void setItems() {
		for (int i = 0; i < leftItems; i++) left.setItem(i, null);
		for (int i = page * leftItems; i < objects.size(); i++) {
			if (i == (page + 1) * leftItems) break;
			T obj = objects.get(i);
			setObjectItem(i - page * leftItems, obj);
		}
	}
	
	protected void setObjectItem(int mainSlot, T object) {
		left.setItem(mainSlot, getItemStack(object));
	}
	
	/**
	 * @param object T object to get the slot from
	 * @return slot in the inventory, -1 if the object is on another page
	 */
	public int getObjectSlot(T object){
		int index = objects.indexOf(object);
		if (index < page * leftItems || index > (page + 1) * leftItems) return -1;
		
		int line = (int) Math.floor(index * 1.0 / barColumn);
		return index + (barRightOffset * line);
	}

	public void updateObjectItem(T object, ItemStack item) {
		int slot = getObjectSlot(object);
		if (slot != -1) super.setItem(slot, item);
	}

	public void removeItem(T existing) {
		if (!objects.remove(existing)) throw new IllegalArgumentException("Cannot remove nonexistent object.");
	}
	
	@Override
	public boolean onClose(Player p) {
		if (objects instanceof Observable observable) observable.unobserve(observableCode);
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
	
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		return true;
	}
	
	private class Left extends GUIView {
		
		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			click(objects.get(slot + page * leftItems), p, click);
			return true;
		}
		
	}
	
	private class Right extends GUIView {
		
		@Override
		public void init() {
			setItem(0, previousPage);
			setItem(getRows() - 1, nextPage);
		}
		
		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			if (slot == 0) {
				if (page != 0) {
					page--;
					setItems();
				}
			}else if (slot == getRows() - 1) {
				if (page + 1 != maxPage) {
					page++;
					setItems();
				}
			}else return onBarItemClick(p, current, slot, click);
			return true;
		}
		
	}
	
}
