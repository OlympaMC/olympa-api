package fr.olympa.api.spigot.auctions.gui;

import java.io.IOException;
import java.sql.SQLException;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.auctions.AuctionsManager;
import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.editor.TextEditor;
import fr.olympa.api.spigot.editor.parsers.NumberParser;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;

public class CreateAuctionGUI extends OlympaGUI {

	private final AuctionsManager manager;

	private double price = -1;
	private int duration = 6; // en demi-journées, défaut = 3 jours

	private boolean finished = false;

	public CreateAuctionGUI(AuctionsManager manager) {
		super("Mettre en vente", 3);
		this.manager = manager;

		ItemStack item = ItemUtils.item(Material.GRAY_STAINED_GLASS_PANE, "§7Dépose l'item à vendre");
		for (int slot : new int[] { 0, 1, 2, 9, 11, 18, 19, 20 }) {
			inv.setItem(slot, item);
		}

		inv.setItem(13, ItemUtils.item(Material.EMERALD, "§aΩ Définis le prix"));
		inv.setItem(14, setDurationItem(ItemUtils.item(Material.CLOCK, "", "§e§l> Clic gauche : §eAugmenter de 12 heures", "§e§l> Clic droit : §eDiminuer de 12 heures")));

		inv.setItem(16, ItemUtils.done);
	}

	private ItemStack setDurationItem(ItemStack item) {
		return ItemUtils.name(item, "§aDurée de l'offre : " + (duration / 2) + " jours" + (duration % 2 == 1 ? " ½" : ""));
	}

	@Override
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		return slot != 10;
	}

	@Override
	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot == 10) return false;
		if (slot == 13) {
			Prefix.DEFAULT_GOOD.sendMessage(p, "Entre le prix auquel tu veux vendre ton objet.");
			new TextEditor<>(p, price -> {
				if (price < 10) {
					Prefix.DEFAULT_BAD.sendMessage(p, "Le prix minimal d'un article est de 10%s.", OlympaMoney.OMEGA);
				}else if (price > 1_000_000_000) {
					Prefix.DEFAULT_BAD.sendMessage(p, "N'exagérons rien...");
				}else {
					this.price = price;
					if (this.price > manager.getPriceMax()) this.price = manager.getPriceMax();
					ItemUtils.lore(current, "", "§ePrix : §6§l" + OlympaMoney.format(this.price));
				}
				create(p);
			}, () -> create(p), false, new NumberParser<>(Double.class, true, true)).enterOrLeave();
		}else if (slot == 14) {
			if (click.isLeftClick()) {
				if (duration < manager.getDemidaysMax()) {
					duration++;
					setDurationItem(current);
				}
			}else if (click.isRightClick()) {
				if (duration > manager.getDemidaysMin()) {
					duration--;
					setDurationItem(current);
				}
			}
		}else if (slot == 16) {
			if (price == -1) {
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1, 1);
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu dois choisir un prix !");
			}else if (inv.getItem(10) == null) {
				p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, SoundCategory.MASTER, 1, 1);
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu dois déposer l'item que tu veux vendre !");
			}else {
				try {
					manager.createAuction(AccountProviderAPI.getter().get(p.getUniqueId()).getInformation(), inv.getItem(10), price, System.currentTimeMillis() + duration * 12 * 3600 * 1000);
					finished = true;
					Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as mis ton item en vente !");
				}catch (SQLException | IOException e) {
					e.printStackTrace();
					Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de la mise en vente de ton item.");
				}finally {
					p.closeInventory();
				}
			}
		}
		return true;
	}

	@Override
	public boolean onClose(Player p) {
		if (!finished) {
			ItemStack item = inv.getItem(10);
			if (item != null) SpigotUtils.giveItems(p, item);
		}
		return true;
	}

}
