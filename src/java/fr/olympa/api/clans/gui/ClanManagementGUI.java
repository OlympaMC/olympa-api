package fr.olympa.api.clans.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.clans.Clan;
import fr.olympa.api.clans.ClanPlayerData;
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

public class ClanManagementGUI<T extends Clan<T, D>, D extends ClanPlayerData<T, D>> extends OlympaGUI {

	private static ItemStack noMember = ItemUtils.skullCustom("§cPas de membre", "ewogICJ0aW1lc3RhbXAiIDogMTU5MTQzNzg2Njk4MywKICAicHJvZmlsZUlkIiA6ICI2MDZlMmZmMGVkNzc0ODQyOWQ2Y2UxZDMzMjFjNzgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfUXVlc3Rpb24iLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCIKICAgIH0KICB9Cn0=");
	private static ItemStack noMemberInvite = ItemUtils.skullCustom("§bInviter un nouveau membre", "ewogICJ0aW1lc3RhbXAiIDogMTU5MTQzNzg2Njk4MywKICAicHJvZmlsZUlkIiA6ICI2MDZlMmZmMGVkNzc0ODQyOWQ2Y2UxZDMzMjFjNzgzOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfUXVlc3Rpb24iLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDM0ZTA2M2NhZmI0NjdhNWM4ZGU0M2VjNzg2MTkzOTlmMzY5ZjRhNTI0MzRkYTgwMTdhOTgzY2RkOTI1MTZhMCIKICAgIH0KICB9Cn0=");

	private ItemStack leave;
	private ItemStack leaveChief;
	private ItemStack disband;

	protected final ClansManager<T, D> manager;
	protected final ClanPlayerInterface<T, D> player;
	protected final D playerData;
	protected final T clan;

	protected boolean isChief;
	private List<D> playersOrder = new ArrayList<>();

	public ClanManagementGUI(ClanPlayerInterface<T, D> p, ClansManager<T, D> manager, int rows) {
		super(manager.stringInventoryManage, rows);
		this.player = p;
		this.manager = manager;
		this.clan = (T) p.getClan();
		this.playerData = clan.getMember(p.getInformation());

		leave = ItemUtils.item(Material.OAK_DOOR, "§c" + manager.stringItemLeave);
		leaveChief = ItemUtils.item(Material.OAK_DOOR, "§c§m" + manager.stringItemLeave, manager.stringItemLeaveChiefLore);
		disband = ItemUtils.item(Material.BARRIER, manager.stringItemDisband);

		refreshInventory();
	}

	protected void refreshInventory() {
		inv.clear();
		playersOrder.clear();

		isChief = playerData.isChief();

		inv.setItem(slotInformations(), getInformationsItem());
		inv.setItem(slotMoney(), ItemUtils.item(Material.EMERALD, "§aMettre de l'argent dans la cagnotte", "§eCagnotte : §6§l" + clan.getMoney().getFormatted()));
		inv.setItem(slotLeave(), isChief ? leaveChief : leave);
		if (isChief) inv.setItem(slotDisband(), disband);

		for (D player : clan.getMembers()) {
			int slot = getPlayerSlot(playersOrder.size());
			Consumer<ItemStack> callback = item -> inv.setItem(slot, item);
			setMemberItem(player, callback);
			playersOrder.add(player);
		}
		for (int id = playersOrder.size(); id < clan.getMaxSize(); id++) {
			inv.setItem(getPlayerSlot(id), isChief ? noMemberInvite : noMember);
		}
	}

	private void setMemberItem(D member, Consumer<ItemStack> callback) {
		if (isChief) {
			String[] lore = member == playerData ? new String[] { "§6§lChef" } : new String[] { "§7Clic §lgauche§r§7 : §cÉjecter", "§7Clic §ldroit§r§7 : §6Transférer la direction" };
			ItemUtils.skull(callback, "§a" + member.getPlayerInformations().getName(), member.getPlayerInformations().getName(), lore);
		}else {
			ItemUtils.skull(callback, "§a" + member.getPlayerInformations().getName(), member.getPlayerInformations().getName(), clan.getChief() == member ? "§6§lChef" : "§eMembre");
		}
	}
	
	private void memberItemClick(Player p, ClickType click, D member) {
		if (member == playerData) return;
		BiConsumer<T, OlympaPlayerInformations> consumer;
		String msg;
		if (click == ClickType.LEFT) {
			consumer = (clan, info) -> clan.removePlayer(info, true);
			msg = String.format(manager.stringSureKick, member.getPlayerInformations().getName());
		}else if (click == ClickType.RIGHT) {
			consumer = Clan::setChief;
			msg = String.format(manager.stringSureChief, member.getPlayerInformations().getName());
		}else return;
		new ConfirmGUI(() -> {
			consumer.accept(clan, member.getPlayerInformations());
			refreshInventory();
			create(p);
		}, () -> {
			this.create(p);
		}, msg).create(p);
	}
	
	protected int getPlayerSlot(int id) {
		return 9 + id;
	}
	
	protected int getPlayerID(int slot) {
		return slot - 9;
	}

	protected int slotInformations() {
		return 4;
	}

	protected int slotMoney() {
		return 8;
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
				new ConfirmGUI(() -> clan.removePlayer(playerData.getPlayerInformations(), true), () -> this.create(p), manager.stringSureLeave).create(p);
			}
		}else if (isChief) {
			int playerID = getPlayerID(slot);
			if (slot >= 0 && playerID < clan.getMaxSize()) {
				if (playersOrder.size() <= playerID) {
					Prefix.DEFAULT.sendMessage(p, "Entre le nom du joueur à inviter.");
					new TextEditor<Player>(p, (target) -> {
						manager.invite(clan, p, target);
						refreshInventory();
						create(p);
					}, () -> this.create(p), false, PlayerParser.PLAYER_PARSER).enterOrLeave();
				}else {
					D member = playersOrder.get(playerID);
					memberItemClick(p, click, member);
				}
			}
		}else if (slot == slotDisband()) {
			new ConfirmGUI(() -> clan.disband(), () -> this.create(p), manager.stringSureDisband, "§cCette action sera définitive.").create(p);
		}else if (slot == slotMoney()) {
			Prefix.DEFAULT.sendMessage(p, "Indique la quantité d'argent que tu veux mettre dans la cagnotte.");
			new TextEditor<>(p, (amount) -> {
				player.getGameMoney().withdraw(amount);
				clan.getMoney().give(amount);
				refreshInventory();
				create(p);
				Prefix.DEFAULT_GOOD.sendMessage(p, String.format(manager.stringAddedMoney, amount));
			}, () -> create(p), false, new MoneyAmountParser(player)).enterOrLeave();
		}
		return true;
	}

}
