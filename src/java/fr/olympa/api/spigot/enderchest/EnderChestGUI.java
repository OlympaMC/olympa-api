package fr.olympa.api.spigot.enderchest;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.OlympaGUI;

public class EnderChestGUI extends OlympaGUI {
	
	private EnderChestPlayerInterface player;
	
	public EnderChestGUI(EnderChestPlayerInterface player) {
		super("Enderchest de " + player.getName(), player.getEnderChestRows());
		this.player = player;
		super.inv.setContents(player.getEnderChestContents());
	}
	
	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		return false;
	}
	
	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return false;
	}
	
	@Override
	public boolean onMoveItem(Player p, ItemStack moved) {
		return false;
	}
	
	@Override
	public boolean noDoubleClick() {
		return false;
	}
	
	@Override
	public boolean noNumberKey() {
		return false;
	}
	
	@Override
	public boolean onClose(Player p) {
		player.setEnderChestContents(inv.getContents());
		return super.onClose(p);
	}
	
}
