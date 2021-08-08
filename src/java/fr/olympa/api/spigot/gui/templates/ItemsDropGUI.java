package fr.olympa.api.spigot.gui.templates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Spliterator;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;

public abstract class ItemsDropGUI extends OlympaGUI {
	
	protected ItemsDropGUI(String name, int rows) {
		super(name, rows + 1);
		
		for (int i = 18; i < 27; i++) {
			inv.setItem(i, i == 22 ? ItemUtils.done : ItemUtils.itemSeparator(DyeColor.PURPLE));
		}
	}
	
	@Override
	public final boolean onMoveItem(Player p, ItemStack moved) {
		return false;
	}
	
	@Override
	public final boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return slot >= inv.getSize() - 9;
	}
	
	@Override
	public final boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == inv.getSize() - 5) {
			if (done(p, click, Arrays.spliterator(inv.getContents(), 0, inv.getSize() - 9))) {
				p.closeInventory();
			}else {
				Inventories.closeAndExit(p);
			}
		}
		return slot >= inv.getSize() - 9;
	}
	
	/**
	 * Called when the player click on the "done" button
	 * @param p
	 * @param click
	 * @param items
	 * @return wether the inventory will give back the items to the player (<code>true</code>) or not.
	 */
	protected abstract boolean done(Player p, ClickType click, Spliterator<ItemStack> items);
	
	@Override
	public final boolean onClose(Player p) {
		List<ItemStack> items = new ArrayList<>();
		for (int i = 0; i < inv.getSize() - 9; i++) {
			ItemStack item = inv.getItem(i);
			if (item != null) items.add(item);
		}
		SpigotUtils.giveItems(p, items.toArray(ItemStack[]::new));
		return true;
	}
	
}
