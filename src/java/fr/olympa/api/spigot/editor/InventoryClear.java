package fr.olympa.api.spigot.editor;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class InventoryClear extends Editor{
	
	private ItemStack[] contents = new ItemStack[0];

	protected InventoryClear(Player p) {
		super(p);
	}

	@Override
	protected void begin() {
		super.begin();
		contents = p.getInventory().getContents();
		p.getInventory().setContents(new ItemStack[0]);
	}

	@Override
	public void end(){
		p.getInventory().setContents(contents);
	}
	
}
