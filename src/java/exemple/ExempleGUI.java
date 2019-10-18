package exemple;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.CustomInventory;
import fr.olympa.api.item.ItemUtils;

public class ExempleGUI implements CustomInventory {

	public Inventory open(Player p) {
		Inventory inv = Bukkit.createInventory(null, 9, "§dNyan");

		inv.setItem(0, ItemUtils.item(Material.GOLD_INGOT, "§eCeci est un item que tu ne peux pas prendre", "lore1", "lore2"));
		inv.setItem(1, ItemUtils.item(Material.DEAD_BUSH, "§aCeci est un item que tu peux prendre", "mais tu ne peux rien poser à la place"));
		inv.setItem(2, ItemUtils.item(Material.SALMON, "§aCeci est un item que tu peux prendre", "et tu peux poser quelque chose à la place"));

		return p.openInventory(inv).getTopInventory();
	}

	public boolean onClick(Player p, Inventory inv, ItemStack current, int slot, ClickType click) {
		if (slot == 1 || slot == 2) return false; // l'item peut être pris
		return true; // l'item ne peut pas être pris
	}

	public boolean onClickCursor(Player p, Inventory inv, ItemStack current, ItemStack cursor, int slot) {
		if (slot == 2) return false; // l'item peut être posé
		return true; // l'item ne peut être posé
	}

	public boolean onClose(Player p, Inventory inv) {
		p.sendMessage("Au revoir");
		return true; // les events de click etc. ne sont plus écoutés pour le joueur
	}

}
