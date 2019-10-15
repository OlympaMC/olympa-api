package fr.olympa.api.gui;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.OlympaCore;
import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.api.utils.SpigotUtils;

public class GuiHandler {

	public static OlympaItemBuild cancelItemBuild = new OlympaItemBuild(Material.REDSTONE_BLOCK, "&4✖ &lImpossible");
	private static List<OlympaGuiBuild> guis = new ArrayList<>();

	public static void cancelInDev(InventoryClickEvent event) {
		cancelItem(event, "En développement");
	}

	public static void cancelItem(InventoryClickEvent event, String msg) {
		event.setCancelled(true);
		Player player = (Player) event.getWhoClicked();
		ItemStack item = event.getCurrentItem();
		Inventory clickedInventory = event.getClickedInventory();

		event.getClickedInventory().setItem(event.getSlot(), cancelItemBuild.lore("", "&c" + msg, "").build());
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

		OlympaCore.getInstance().getTask().runTaskLater(player.getUniqueId() + String.valueOf(event.getSlot()), () -> {
			if (clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
				clickedInventory.setItem(event.getSlot(), item);
			}
		}, 30);
	}

	public static OlympaGuiBuild getGui(Player player) {
		return guis.stream().filter(data -> SpigotUtils.isSamePlayer(player, data.getPlayer())).findFirst().orElse(null);
	}

	public static void removeGui(OlympaGuiBuild guiData) {
		guis.remove(guiData);
	}

	public static void removeGui(Player player) {
		removeGui(getGui(player));

	}

	protected static void setGui(OlympaGuiBuild guiData, Player player) {
		OlympaGuiBuild guiDataOld = getGui(player);
		if (guiDataOld != null) {
			removeGui(guiDataOld);
		}
		guis.add(guiData);
	}

}
