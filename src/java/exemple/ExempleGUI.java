package exemple;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.item.ItemUtils;

public class ExempleGUI extends OlympaGUI {

	public ExempleGUI() {
		super("Exemple", 1);
		inv.setItem(0, ItemUtils.item(Material.GOLD_INGOT, "§eCeci est un item que tu ne peux pas prendre", "lore1", "lore2"));
		inv.setItem(1, ItemUtils.item(Material.DEAD_BUSH, "§aCeci est un item que tu peux prendre", "mais tu ne peux rien poser à la place"));
		inv.setItem(2, ItemUtils.item(Material.SALMON, "§aCeci est un item que tu peux prendre", "et tu peux poser quelque chose à la place"));
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 1 || slot == 2) {
			return false; // l'item peut être pris
		}
		return true; // l'item ne peut pas être pris
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		if (slot == 2) {
			return false; // l'item peut être posé
		}
		return true; // l'item ne peut être posé
	}

	@Override
	public boolean onClose(Player p) {
		p.sendMessage("Au revoir");
		return true; // les events de click etc. ne sont plus écoutés pour le joueur
	}

}
