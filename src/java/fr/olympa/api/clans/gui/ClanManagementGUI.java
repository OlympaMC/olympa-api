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
import fr.olympa.api.editor.parsers.MoneyAmountParser;
import fr.olympa.api.editor.parsers.PlayerParser;
import fr.olympa.api.gui.OlympaGUI;
import fr.olympa.api.gui.templates.ConfirmGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.utils.Prefix;

public class ClanManagementGUI<T extends Clan<T>> extends OlympaGUI {

	private static ItemStack noMember = ItemUtils.skull("§cPas de membre", "MHF_Question");
	private static ItemStack noMemberInvite = ItemUtils.skull("§bInviter un nouveau membre", "MHF_Question");
	private static ItemStack addMoney = ItemUtils.item(Material.EMERALD, "§aMettre de l'argent dans la cagnotte");

	private ItemStack leave;
	private ItemStack leaveChief;
	private ItemStack disband;

	private final ClanPlayerInterface<T> player;
	private final OlympaPlayerInformations playerInformations;
	private final T clan;

	private boolean isChief;
	private List<OlympaPlayerInformations> playersOrder = new ArrayList<>();
	private ClansManager<T> manager;

	public ClanManagementGUI(ClanPlayerInterface<T> p, ClansManager<T> manager, int rows) {
		super(manager.stringInventoryManage, rows);
		this.player = p;
		this.manager = manager;
		this.playerInformations = p.getInformation();
		this.clan = (T) p.getClan();

		leave = ItemUtils.item(Material.OAK_DOOR, "§c" + manager.stringItemLeave);
		leaveChief = ItemUtils.item(Material.OAK_DOOR, "§c§m" + manager.stringItemLeave, manager.stringItemLeaveChiefLore);
		disband = ItemUtils.item(Material.BARRIER, manager.stringItemDisband);

		refreshInventory();
	}

	protected void refreshInventory() {
		inv.clear();
		playersOrder.clear();

		isChief = clan.getChief() == playerInformations;

		inv.setItem(slotInformations(), getInformationsItem());
		inv.setItem(slotMoney(), addMoney);
		inv.setItem(slotLeave(), isChief ? leaveChief : leave);
		if (isChief) inv.setItem(slotDisband(), disband);

		for (Entry<OlympaPlayerInformations, ClanPlayerInterface<T>> entry : clan.getMembers()) {
			OlympaPlayerInformations member = entry.getKey();
			ItemStack item;
			if (isChief) {
				String[] lore = member == playerInformations ? new String[] { "§6§lChef" } : new String[] { "§7Clic §lgauche§r§7 : §cÉjecter", "§7Clic §ldroit§r§7 : §6Transférer la direction" };
				item = ItemUtils.skull("§a" + member.getName(), member.getName(), lore);
			}else {
				item = ItemUtils.skull("§a" + member.getName(), member.getName(), clan.getChief() == member ? "§6§lChef" : "§eMembre");
			}
			inv.setItem(slotPlayerFirst() + playersOrder.size(), item);
			playersOrder.add(member);
		}
		for (int id = playersOrder.size(); id < clan.getMaxSize(); id++) {
			inv.setItem(slotPlayerFirst() + id, isChief ? noMemberInvite : noMember);
		}
	}

	protected int slotInformations() {
		return 4;
	}

	protected int slotMoney() {
		return 8;
	}

	protected int slotPlayerFirst() {
		return 9;
	}

	protected int slotDisband() {
		return 16;
	}

	protected int slotLeave() {
		return 17;
	}

	protected ItemStack getInformationsItem() {
		return ItemUtils.item(Material.FILLED_MAP, "§eInformations sur le clan §6" + clan.getName(), "§e§lNombre de membres : §e§o" + clan.getMembersAmount() + "/" + clan.getMaxSize(), "§e§lCagnotte : §6" + clan.getMoney().getFormatted());
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == slotLeave()) {
			if (!isChief) {
				new ConfirmGUI(() -> clan.removePlayer(playerInformations, true), () -> this.create(p), manager.stringSureLeave);
				p.closeInventory();
			}
		}else if (isChief && slot >= slotPlayerFirst() && slot < slotPlayerFirst() + clan.getMaxSize()) {
			if (playersOrder.size() <= slot - slotPlayerFirst()) {
				Prefix.DEFAULT.sendMessage(p, "Entre le nom du joueur à inviter.");
				new TextEditor<Player>(p, (target) -> {
					manager.invite(clan, p, target);
					refreshInventory();
					create(p);
				}, () -> this.create(p), false, PlayerParser.PLAYER_PARSER).enterOrLeave();
			}else {
				OlympaPlayerInformations member = playersOrder.get(slot - slotPlayerFirst());
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
					refreshInventory();
					create(p);
				}, () -> {
					this.create(p);
				}, msg).create(p);
			}
		}else if (slot == slotDisband()) {
			new ConfirmGUI(() -> clan.disband(), () -> this.create(p), manager.stringSureDisband, "§cCette action sera définitive.").create(p);
		}else if (slot == slotMoney()) {
			new TextEditor<>(p, (amount) -> {
				create(p);
				player.getGameMoney().withdraw(amount);
				clan.getMoney().give(amount);
				inv.setItem(4, getInformationsItem()); // pour update la money
				Prefix.DEFAULT_GOOD.sendMessage(p, String.format(manager.stringAddedMoney, amount));
			}, () -> create(p), false, new MoneyAmountParser(player)).enterOrLeave();
		}
		return true;
	}

}
