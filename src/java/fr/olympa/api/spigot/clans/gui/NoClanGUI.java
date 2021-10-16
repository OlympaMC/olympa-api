package fr.olympa.api.spigot.clans.gui;

import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.clans.Clan;
import fr.olympa.api.spigot.clans.ClanPlayerData;
import fr.olympa.api.spigot.clans.ClansManager;
import fr.olympa.api.spigot.editor.TextEditor;
import fr.olympa.api.spigot.gui.Inventories;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;

public class NoClanGUI<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> extends OlympaGUI {

	private ClansManager<T, D> manager;

	public NoClanGUI(Player p, ClansManager<T, D> manager) {
		super(manager.stringInventoryJoin, InventoryType.HOPPER);
		this.manager = manager;

		inv.setItem(1, ItemUtils.item(Material.CRAFTING_TABLE, manager.stringItemCreate));
		inv.setItem(3, ItemUtils.item(Material.PAPER, "§aVoir mes invitations", "§7§o" + manager.getPlayerInvitations(p).size() + " invitations en attente"));
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		switch (slot) {
		case 1:
			Prefix.DEFAULT.sendMessage(p, manager.stringChooseName);
			new TextEditor<String>(p, (msg) -> {
				if (!manager.checkName(p, msg)) {
					Inventories.closeAndExit(p);
					return;
				}
				try {
					manager.createClan(AccountProviderAPI.getter().get(p.getUniqueId()), msg, manager.generateTag(msg));
				}catch (SQLException e) {
					e.printStackTrace();
					Inventories.closeAndExit(p);
					Prefix.ERROR.sendMessage(p, "Une erreur est survenue.");
				}
				manager.provideManagementGUI(AccountProviderAPI.getter().get(p.getUniqueId())).create(p);
			}, () -> {}, false, (player, msg) -> {
				if (!manager.checkName(p, msg)) return null;
				return msg;
			}).enterOrLeave();
			break;
		case 3:
			new InvitationsView<>(p, manager).toGUI().create(p);
			break;
		}
		return true;
	}

}
