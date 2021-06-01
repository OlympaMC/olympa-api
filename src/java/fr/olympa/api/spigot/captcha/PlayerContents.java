package fr.olympa.api.spigot.captcha;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.spigot.config.CustomConfig;

public class PlayerContents {

	public static void clearInventory(PlayerInventory inventory) {
		inventory.clear();
		inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
	}

	public PlayerContents fromDisk(Player player) {
		String uuid = player.getUniqueId().toString();

		Object itemContents = config.get(uuid + ".contents");
		ItemStack[] inventoryItemContents = null;
		if (itemContents != null)
			if (itemContents instanceof ItemStack[])
				inventoryItemContents = (ItemStack[]) itemContents;
			else {
				LinkSpigotBungee.Provider.link.sendMessage("An error as occured with " + player.getName() + "'s inventory: his stuff is lost ...");
				return new PlayerContents(config, player.getUniqueId(), null);
			}

		ItemStack[] inventoryArmorContents = null;
		if (inventoryItemContents != null || inventoryArmorContents != null) {
			config.set(uuid + ".contents", (Object) null);
			config.save();
		}

		return new PlayerContents(config, player.getUniqueId(), inventoryItemContents);
	}

	private UUID uuid;
	private ItemStack[] inventoryItemContents;
	CustomConfig config;

	public PlayerContents(CustomConfig config, Player player) {
		this.config = config;
		uuid = player.getUniqueId();
		inventoryItemContents = player.getInventory().getContents();
	}

	private PlayerContents(CustomConfig config, UUID uuid, ItemStack[] inventoryItemContents) {
		this.config = config;
		this.uuid = uuid;
		this.inventoryItemContents = inventoryItemContents;
	}

	public void clearInventory() {
		Player player = getPlayer();
		PlayerContents.clearInventory(player.getInventory());
	}

	public ItemStack[] getInventoryItemContents() {
		return inventoryItemContents;
	}

	private Player getPlayer() {
		return Bukkit.getPlayer(uuid);
	}

	public UUID getUniqueId() {
		return uuid;
	}

	public boolean hasData() {
		return inventoryItemContents != null && inventoryItemContents.length != 0;
	}

	public void returnHisInventory() {
		PlayerInventory inventory = getPlayer().getInventory();
		clearInventory(inventory);
		if (inventoryItemContents != null)
			inventory.setContents(inventoryItemContents);
		clearData();
	}

	public void clearData() {
		inventoryItemContents = null;
		saveToDisk();
	}

	public void saveToDisk() {
		if (inventoryItemContents == null)
			config.set(uuid.toString(), (Object) null);
		else
			config.set(uuid.toString() + ".contents", inventoryItemContents);
		config.save();
	}
}
