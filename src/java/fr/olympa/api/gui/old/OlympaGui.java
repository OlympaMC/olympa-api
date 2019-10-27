package fr.olympa.api.gui.old;

import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.utils.SpigotUtils;

@Deprecated
public class OlympaGui {

	private static int columns = 9;

	private Object data;
	private Inventory inventory;
	private Player player;

	public OlympaGui(String name, int column) {
		int size = columns * column;
		this.inventory = Bukkit.createInventory(null, size, SpigotUtils.color(name));
	}

	public OlympaGui(String name, InventoryType type) {
		this.inventory = Bukkit.createInventory(null, type, SpigotUtils.color(name));
		this.data = "";
	}

	public OlympaGui(String name, String id, long size) {
		if (size % columns != 0) {
			size = size + columns - size % columns;
		}
		this.inventory = Bukkit.createInventory(null, (int) size, SpigotUtils.color(name));
		this.data = "";
	}

	public OlympaGui(String name, String id, long size, Object data) {
		if (size % columns != 0) {
			size = size + columns - size % columns;
		}
		this.inventory = Bukkit.createInventory(null, (int) size, SpigotUtils.color(name));
		this.data = data;
	}

	public void addItem(ItemStack item) {
		int index = IntStream.range(0, this.inventory.getContents().length).filter(i -> this.inventory.getContents()[i] == null).findFirst().orElse(-1);
		this.inventory.setItem(index, item);
	}

	public int getColumn() {
		return columns;
	}

	public int getColumn(int i) {
		return this.getSlot(1, i);
	}

	public Object getData() {
		return this.data;
	}

	public int getFirstSlot() {
		return 0;
	}

	public Inventory getInventory() {
		return this.inventory;
	}

	public ItemStack getItem(int index) {
		return this.inventory.getItem(index);
	}

	public int getLastSlot() {
		return this.getSize() - 1;
	}

	public int getMiddleColumn() {
		return (columns - 1) * 2;
	}

	public int getMiddleLigne(int i) {
		return this.getColumn(1) / 2 * i;
	}

	public int getMiddleSlot() {
		return this.getSize() / 2;
	}

	public int getMiddleSlotPlusColumn(int i) {
		return this.getMiddleSlot() + i * 9;
	}

	@Deprecated
	public String getName() {
		return this.inventory.getName();
	}

	public Player getPlayer() {
		return this.player;
	}

	public int getSize() {
		return this.inventory.getSize();
	}

	// TODO à verif
	public int getSlot(int ligne, int column) {
		return columns * (ligne - 1) + 1 + column;
	}

	public void openInventory(Player player) {
		player.openInventory(this.inventory);
	}

	public void removeItem(int index) {
		this.inventory.setItem(index, null);
	}

	public void setData(Object data) {
		this.data = data;
	}

	public void setItem(int index, ItemStack item) {
		this.inventory.setItem(index, item);
	}

	public void setItem(ItemStack[] items) {
		this.inventory.setContents(items);
	}
}
