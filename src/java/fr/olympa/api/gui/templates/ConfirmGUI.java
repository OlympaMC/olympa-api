package fr.olympa.api.gui.templates;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.core.spigot.OlympaCore;

public class ConfirmGUI extends OlympaGUI {

	private Runnable yes, no;

	public ConfirmGUI(Runnable yes, Runnable no, String indication) {
		this(yes, no, indication, null);
	}

	public ConfirmGUI(Runnable yes, Runnable no, String indication, String lore) {
		super("Confirmer ?", InventoryType.HOPPER);
		this.yes = yes;
		this.no = no;

		this.inv.setItem(1, ItemUtils.item(Material.LIME_DYE, "§aOui"));
		this.inv.setItem(2, ItemUtils.item(Material.PAPER, indication, lore));
		this.inv.setItem(3, ItemUtils.item(Material.RED_DYE, "§cNon"));
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		Inventories.closeAndExit(p);
		if (slot == 1) {
			this.yes.run();
		} else if (slot == 3) {
			this.no.run();
		}
		return true;
	}

	@Override
	public boolean onClose(Player p) {
		new BukkitRunnable() {
			@Override
			public void run() {
				ConfirmGUI.this.no.run();
			}
		}.runTask(OlympaCore.getInstance());
		return false;
	}

}
