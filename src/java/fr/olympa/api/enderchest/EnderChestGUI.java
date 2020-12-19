package fr.olympa.api.enderchest;

import org.bukkit.entity.Player;

import fr.olympa.api.gui.OlympaGUI;

public class EnderChestGUI extends OlympaGUI {
	
	private EnderChestPlayerInterface player;
	
	public EnderChestGUI(EnderChestPlayerInterface player) {
		super("Enderchest de " + player.getName(), player.getEnderChestRows());
		this.player = player;
		super.inv.setContents(player.getEnderChestContents());
	}
	
	@Override
	public boolean onClose(Player p) {
		player.setEnderChestContents(inv.getContents());
		return super.onClose(p);
	}
	
}
