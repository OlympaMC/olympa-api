package fr.olympa.api.clans.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerData;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.gui.Inventories;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;

public class InvitationsGUI<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> extends PagedGUI<T> {

	private ClansManager<T, D> manager;

	protected InvitationsGUI(Player p, ClansManager<T, D> manager) {
		super("§4Mes invitations", DyeColor.MAGENTA, manager.getPlayerInvitations(p), 5);
		this.manager = manager;
	}

	public ItemStack getItemStack(T clan) {
		return ItemUtils.item(Material.PAPER, "§a" + clan.getName());
	}

	public void click(T existing, Player p) {
		if (existing.addPlayer(AccountProvider.get(p.getUniqueId()))) {
			Inventories.closeAndExit(p);
			manager.clearPlayerInvitations(p);
		}else {
			Prefix.DEFAULT_BAD.sendMessage(p, manager.stringClanFull);
		}
	}

}
