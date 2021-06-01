	package fr.olympa.api.spigot.gui.templates;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;

/**
 * An inventory which has up to 54 slots to store items. Each item is linked in a list to an instance of type T.
 * @author SkytAsul
 *
 * @param <T> type of objects stocked in the list
 */
public abstract class ListGUI<T> extends OlympaGUI {

	protected List<T> objects;
	protected int size;
	
	protected Player p;
	
	public ListGUI(String name, int rows, List<T> list) {
		super(name, rows);
		this.objects = list;
		this.size = rows * 9;
		
		inv.setItem(size - 1, ItemUtils.done);
		for (int i = 0; i < 8; i++){
			if (objects.size() <= i){
				inv.setItem(i, ItemUtils.none);
			}else {
				inv.setItem(i, getItemStack(objects.get(i)));
			}
		}
	}
	
	public void remove(int slot){
		objects.remove(slot);
		for (int i = slot; i <= objects.size(); i++){
			inv.setItem(i, i == objects.size() ? ItemUtils.none : inv.getItem(i + 1));
		}
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == size - 1){
			finish();
		}else {
			if (current.equals(ItemUtils.none)) {
				click(null);
			}else if (click == ClickType.MIDDLE){
				remove(slot);
			}else {
				T obj = objects.get(slot);
				remove(slot);
				click(obj);
			}
		}
		return true;
	}
	
	/**
	 * Call this when an object is ready to be inserted in the list
	 * @param object Object to put
	 * @return ItemStack created with {@link #getItemStack(Object)}
	 */
	public ItemStack finishItem(T object){
		Inventories.create(p, this);
		objects.add(object);
		int slot = objects.size() - 1;
		inv.setItem(slot, getItemStack(object));
		return inv.getItem(slot);
	}
	
	/**
	 * @param object existing object to represent
	 * @return ItemStack who represents the object
	 */
	public abstract ItemStack getItemStack(T object);
	
	/**
	 * Called when an object is clicked
	 * @param existing clicked object (may be null if there was no previous object)
	 */
	public abstract void click(T existing);
	
	/**
	 * Called when the player hit the finish button
	 */
	public abstract void finish();
	
}
