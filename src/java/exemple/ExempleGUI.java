package exemple;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.CustomInventory;
import fr.olympa.api.gui.OlympaGui;
import fr.olympa.api.item.ItemUtils;

public class ExempleGUI implements CustomInventory {

	@Override
	public boolean onClick(Player p, OlympaGui gui, ItemStack current, int slot, ClickType click) {
		if (slot == 1 || slot == 2) {
			return false; // l'item peut être pris
		}
		return true; // l'item ne peut pas être pris
	}

	@Override
	public boolean onClickCursor(Player p, OlympaGui gui, ItemStack current, ItemStack cursor, int slot) {
		if (slot == 2) {
			return false; // l'item peut être posé
		}
		return true; // l'item ne peut être posé
	}

	@Override
	public boolean onClose(Player p, OlympaGui inv) {
		p.sendMessage("Au revoir");
		return true; // les events de click etc. ne sont plus écoutés pour le joueur
	}

	@Override
	public OlympaGui open(Player p) {
		OlympaGui gui = new OlympaGui("Name", 3);

		gui.setItem(0, ItemUtils.item(Material.GOLD_INGOT, "§eCeci est un item que tu ne peux pas prendre", "lore1", "lore2"));
		gui.setItem(1, ItemUtils.item(Material.DEAD_BUSH, "§aCeci est un item que tu peux prendre", "mais tu ne peux rien poser à la place"));
		gui.setItem(2, ItemUtils.item(Material.SALMON, "§aCeci est un item que tu peux prendre", "et tu peux poser quelque chose à la place"));

		gui.openInventory(p);

		return gui;
	}

}
