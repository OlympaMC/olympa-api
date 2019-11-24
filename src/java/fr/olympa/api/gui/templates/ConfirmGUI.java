package fr.olympa.api.gui.templates;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.OlympaCore;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;

public class ConfirmGUI extends OlympaGUI {

	private Runnable yes, no;
	
	public ConfirmGUI(Runnable yes, Runnable no, String indication) {
		this(yes, no, indication, null);
	}
	
	public ConfirmGUI(Runnable yes, Runnable no, String indication, String lore) {
		super("Confirmer ?", InventoryType.HOPPER);
		this.yes = yes;
		this.no = no;
		
		inv.setItem(1, ItemUtils.item(Material.LIME_DYE, "§aOui"));
		inv.setItem(2, ItemUtils.item(Material.PAPER, indication, lore));
		inv.setItem(3, ItemUtils.item(Material.ROSE_RED, "§cNon"));
	}

	public Inventory open(Player p) {
		
		return p.openInventory(inv).getTopInventory();
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		Inventories.closeAndExit(p);
		if (slot == 1) {
			yes.run();
		}else if (slot == 3) {
			no.run();
		}
		return true;
	}

	public boolean onClose(Player p) {
		new BukkitRunnable() {
			public void run() {
				no.run();
			}
		}.runTask(OlympaCore.getInstance());
		return false;
	}

}
