package fr.olympa.api.clans.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerInterface;
import fr.olympa.api.clans.ClansManager;
import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.editor.parsers.PlayerParser;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.gui.templates.ConfirmGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.objects.OlympaPlayerInformations;
import fr.olympa.api.utils.Prefix;

public class ClanManagementGUI<T extends Clan<T>> extends OlympaGUI {

	private static ItemStack noMember = ItemUtils.skullCustom("§cPas de membre", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIzZWFlZmJkNTgxMTU5Mzg0Mjc0Y2RiYmQ1NzZjZWQ4MmViNzI0MjNmMmVhODg3MTI0ZjllZDMzYTY4NzJjIn19fQ==");
	private static ItemStack noMemberInvite = ItemUtils.skullCustom("§bInviter un nouveau membre", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDIzZWFlZmJkNTgxMTU5Mzg0Mjc0Y2RiYmQ1NzZjZWQ4MmViNzI0MjNmMmVhODg3MTI0ZjllZDMzYTY4NzJjIn19fQ==");

	private static ItemStack leave = ItemUtils.item(Material.OAK_DOOR, "§cQuitter le clan");
	private static ItemStack leaveChief = ItemUtils.item(Material.OAK_DOOR, "§c§mQuitter le clan", "§7§oPour pouvoir quitter votre clan,", "§7§ovous devez tout d'abord", "§7§otransmettre la direction de celui-ci", "§7§oà un autre membre.");

	private static ItemStack disband = ItemUtils.item(Material.BARRIER, "§cDémenteler le clan");

	private ClanPlayerInterface<T> player;
	private OlympaPlayerInformations playerInformations;
	private T clan;
	private boolean isChief;

	private List<OlympaPlayerInformations> playersOrder = new ArrayList<>();
	private ClansManager<T> manager;

	public ClanManagementGUI(ClanPlayerInterface<T> p, ClansManager<T> manager) {
		super(manager.stringInventoryManage, 2);
		this.player = p;
		this.manager = manager;
		this.playerInformations = p.getInformation();
		this.clan = (T) p.getClan();
		isChief = clan.getChief() == playerInformations;

		inv.setItem(4, getInformationsItem());
		inv.setItem(17, isChief ? leaveChief : leave);
		if (isChief) inv.setItem(16, disband);

		for (Entry<OlympaPlayerInformations, ClanPlayerInterface<T>> entry : clan.getMembers()) {
			OlympaPlayerInformations member = entry.getKey();
			playersOrder.add(member);
			ItemStack item;
			if (isChief) {
				String[] lore = member == playerInformations ? new String[] { "§6§lChef" } : new String[] { "§7Clic §lgauche§r§7 : §cÉjecter", "§7Clic §ldroit§r§7 : §6Transférer la direction" };
				item = ItemUtils.skull("§a" + member.getName(), member.getName(), lore);
			}else {
				item = ItemUtils.skull("§a" + member.getName(), member.getName(), clan.getChief() == member ? "§6§lChef" : "§eMembre");
			}
			inv.setItem(8 + playersOrder.size(), item);
		}
		for (int id = playersOrder.size(); id < clan.getMaxSize(); id++) {
			inv.setItem(9 + id, isChief ? noMemberInvite : noMember);
		}
	}

	protected ItemStack getInformationsItem() {
		return ItemUtils.item(Material.FILLED_MAP, "§eInformations sur le clan §6" + clan.getName(), "§e§lNombre de membres §r§6: §e§o" + clan.getMembersAmount());
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 17) {
			if (!isChief) {
				new ConfirmGUI(() -> clan.removePlayer(playerInformations, true), () -> this.create(p), manager.stringSureLeave);
				p.closeInventory();
			}
		}else if (isChief && slot >= 9 && slot < 14) {
			if (playersOrder.size() <= slot - 9) {
				Prefix.DEFAULT.sendMessage(p, "Entrez le nom du joueur à inviter.");
				new TextEditor<Player>(p, (target) -> {
					manager.invite(clan, p, target);
					new ClanManagementGUI<T>(player, manager).create(p);
				}, () -> this.create(p), false, new PlayerParser()).enterOrLeave(p);
			}else {
				OlympaPlayerInformations member = playersOrder.get(slot - 9);
				if (member == playerInformations) return true;
				BiConsumer<T, OlympaPlayerInformations> consumer;
				String msg;
				if (click == ClickType.LEFT){
					consumer = (clan, info) -> clan.removePlayer(info, true);
					msg = String.format(manager.stringSureKick, member.getName());
				}else if (click == ClickType.RIGHT) {
					consumer = Clan::setChief;
					msg = String.format(manager.stringSureChief, member.getName());
				}else return true;
				new ConfirmGUI(() -> {
					consumer.accept(clan, member);
					new ClanManagementGUI<T>(player, manager).create(p); // pour update les items
				}, () -> {
					this.create(p);
				}, msg).create(p);
			}
		}else if (slot == 16) {
			new ConfirmGUI(() -> clan.disband(), () -> this.create(p), manager.stringSureDisband, "§cCette action sera définitive.").create(p);
		}
		return true;
	}

}
