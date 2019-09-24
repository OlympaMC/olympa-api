package fr.tristiisch.olympa.api.gui;

import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.tristiisch.olympa.api.utils.SpigotUtils;

public class OlympaGuiBuild {

	private static int columns = 9;

	private String id;

	private String data;
	private Inventory inventory;
	private Player player;
	private boolean canClick = false;

	public OlympaGuiBuild(String name, String id, int column) {
		int size = columns * column;
		this.inventory = Bukkit.createInventory(null, size, SpigotUtils.color(name));
		this.id = id;
		this.data = "";
	}

	public OlympaGuiBuild(String name, String id, int size, String data) {
		if (size % columns != 0) {
			size = size + columns - size % columns;
		}
		this.inventory = Bukkit.createInventory(null, size, SpigotUtils.color(name));
		this.id = id;
		this.data = data;
	}

	public OlympaGuiBuild(String name, String id, InventoryType type) {
		this.inventory = Bukkit.createInventory(null, type, SpigotUtils.color(name));
		this.id = id;
		this.data = "";
	}

	public OlympaGuiBuild(String name, String id, long size) {
		if (size % columns != 0) {
			size = size + columns - size % columns;
		}
		this.inventory = Bukkit.createInventory(null, (int) size, SpigotUtils.color(name));
		this.id = id;
		this.data = "";
	}

	public void addItem(ItemStack item) {
		int index = IntStream.range(0, this.inventory.getContents().length).filter(i -> this.inventory.getContents()[i] == null).findFirst().orElse(-1);
		this.inventory.setItem(index, item);
	}

	public boolean canClick() {
		return this.canClick;
	}

	public int getColumn() {
		return columns;
	}

	public int getColumn(int i) {
		return this.getSlot(1, i);
	}

	public String getData() {
		return this.data;
	}

	public int getFirstSlot() {
		return 0;
	}

	public String getId() {
		return this.id;
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
		this.player = player;
		player.openInventory(this.inventory);
		if (this.id != null) {
			GuiHandler.setGui(this, player);
		}
	}

	public void removeItem(int index) {
		this.inventory.setItem(index, null);
	}

	public void setCanClick(boolean canClick) {
		this.canClick = canClick;
	}

	public void setData(String data) {
		this.data = data;
	}

	public void setItem(int index, ItemStack item) {
		this.inventory.setItem(index, item);
	}

	public void setItem(ItemStack[] items) {
		this.inventory.setContents(items);
	}
}
