package fr.olympa.api.spigot.enderchest;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.player.OlympaPlayer;

public interface EnderChestPlayerInterface extends OlympaPlayer {
	
	public ItemStack[] getEnderChestContents();
	
	public void setEnderChestContents(ItemStack[] contents);
	
	public int getEnderChestRows();
	
}
