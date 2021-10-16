package fr.olympa.api.spigot.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public abstract class GUIView {
	
	private GUIChanger gui;
	
	public void setGUI(GUIChanger gui) {
		this.gui = gui;
		init();
	}
	
	public void init() {}
	
	public void setItem(int slot, ItemStack item) {
		gui.setItem(slot, item);
	}
	
	public ItemStack getItem(int slot) {
		return gui.getItem(slot);
	}
	
	public void clear() {
		gui.clear();
	}
	
	public int getRows() {
		return gui.getRows();
	}
	
	public int getColumns() {
		return gui.getColumns();
	}
	
	public int getSize() {
		return gui.getSize();
	}
	
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return true;
	}
	
	public boolean onClose(Player p) {
		return true;
	}
	
	public OlympaGUI toGUI(String name, int rows) {
		return new ViewGUI(name, this, rows);
	}
	
}
