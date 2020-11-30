package fr.olympa.api.auctions.gui;

import java.sql.SQLException;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.auctions.Auction;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class MyAuctionsGUI extends PagedGUI<Auction> {

	private MoneyPlayerInterface player;
	private AuctionsManager manager;

	public MyAuctionsGUI(AuctionsManager manager, MoneyPlayerInterface player) {
		super("Mes articles", DyeColor.LIGHT_BLUE, manager.getAllAuctions().stream().filter(x -> x.player.equals(player.getInformation())).collect(Collectors.toList()), 4);
		super.setBarItem(1, ItemUtils.item(Material.CHEST, "§a← Revenir aux ventes", "§8> §7" + manager.getOngoingAuctions().size() + " enchères en cours"));
		super.setBarItem(2, ItemUtils.item(Material.DIAMOND, "§b✦ Vendre un objet", "§8> §7" + (manager.getMaxAuctions(player) - objects.size()) + " enchères disponibles"));
		this.manager = manager;
		this.player = player;
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		ItemStack item = object.getShownItem();
		ItemUtils.loreAdd(item, "");
		if (object.bought) {
			ItemUtils.loreAdd(item, "§6§lVendu !", "§e> §oClique pour récupérer tes gains");
		}else if (object.hasExpired()) {
			ItemUtils.loreAdd(item, "§6§lExpiré !", "§e> §oClique pour récupérer ton objet");
		}else {
			ItemUtils.loreAdd(item, "§6§lEn attente...", "§c> §oClique pour annuler la vente");
		}
		return item;
	}

	@Override
	public void click(Auction existing, Player p) {
		if (existing.bought) {
			try {
				manager.terminateAuction(existing);
				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as reçu %s (taxes retirées).", OlympaMoney.format(manager.getTaxManager().pay(player, existing.price)));
			}catch (SQLException e) {
				e.printStackTrace();
				Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de ton paiement.");
			}
		}else {
			try {
				manager.terminateAuction(existing);
				SpigotUtils.giveItems(p, existing.item);
				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as clôturé ta vente.");
			}catch (SQLException e) {
				e.printStackTrace();
				Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de la suppression de cette vente.");
			}
		}
		p.closeInventory();
	}
	
	@Override
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		if (barSlot == 1) {
			manager.openAuctionsGUI(p);
		}else if (barSlot == 2) {
			if (manager.getAllAuctions().stream().filter(auction -> auction.player.equals(player.getInformation())).count() > manager.getMaxAuctions(player)) {
				Prefix.DEFAULT_BAD.sendMessage(p, "Tu as atteint la limite des %d ventes ! Récupères tes gains en attente et patiente pour que tes autres objets se vendent.", manager.getMaxAuctions(player));
			}else manager.openAuctionCreationGUI(p);
		}
		return true;
	}

}
