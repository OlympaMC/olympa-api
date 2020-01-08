package fr.olympa.api.gui;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.item.OlympaItemBuild;
import fr.olympa.core.spigot.OlympaCore;

public class GuiHandler {

	public static OlympaItemBuild cancelItemBuild = new OlympaItemBuild(Material.REDSTONE_BLOCK, "&4✖ &lImpossible");

	public static void cancelItem(Player player, ItemStack item, Inventory clickedInventory, int slot) {
		cancelItem(player, item, clickedInventory, slot, "En développement");
	}

	public static void cancelItem(Player player, ItemStack item, Inventory clickedInventory, int slot, String msg) {
		clickedInventory.setItem(slot, cancelItemBuild.lore("", "&c" + msg, "").build());
		player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1, 1);

		OlympaCore.getInstance().getTask().runTaskLater(player.getUniqueId() + String.valueOf(slot), () -> {
			if (clickedInventory.equals(player.getOpenInventory().getTopInventory())) {
				clickedInventory.setItem(slot, item);
			}
		}, 30);
	}
}
