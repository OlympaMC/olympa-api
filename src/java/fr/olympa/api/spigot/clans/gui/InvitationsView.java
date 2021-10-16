package fr.olympa.api.spigot.clans.gui;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.clans.Clan;
import fr.olympa.api.spigot.clans.ClanPlayerData;
import fr.olympa.api.spigot.clans.ClansManager;
import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.gui.templates.PagedView;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;

public class InvitationsView<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> extends PagedView<T> {

	private ClansManager<T, D> manager;

	protected InvitationsView(Player p, ClansManager<T, D> manager) {
		super(DyeColor.MAGENTA, manager.getPlayerInvitations(p));
		this.manager = manager;
	}

	@Override
	public ItemStack getItemStack(T clan) {
		return ItemUtils.item(Material.PAPER, "§a" + clan.getNameAndTag());
	}

	@Override
	public void click(T existing, Player p, ClickType click) {
		if (existing.addPlayer(AccountProviderAPI.getter().get(p.getUniqueId()), true)) {
			Inventories.closeAndExit(p);
			manager.clearPlayerInvitations(p);
		}else {
			Prefix.DEFAULT_BAD.sendMessage(p, manager.stringClanFull);
		}
	}
	
	public OlympaGUI toGUI() {
		return super.toGUI("§4Mes invitations", 5);
	}

}
