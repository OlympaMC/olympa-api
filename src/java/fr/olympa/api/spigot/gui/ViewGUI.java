package fr.olympa.api.spigot.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

public class ViewGUI extends OlympaGUI {
	
	private GUIView view;
	
	public ViewGUI(String name, GUIView view, int rows) {
		super(name, rows);
		this.view = view;
		view.setGUI(this);
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return view.onClick(p, current, slot, click);
	}
	
	@Override
	public boolean onClose(Player p) {
		return view.onClose(p);
	}
	
}
