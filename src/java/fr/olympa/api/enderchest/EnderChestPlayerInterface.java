package fr.olympa.api.enderchest;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.player.OlympaPlayer;

public interface EnderChestPlayerInterface extends OlympaPlayer {
	
	public ItemStack[] getEnderChestContents();
	
	public void setEnderChestContents(ItemStack[] contents);
	
	public int getEnderChestRows();
	
}
