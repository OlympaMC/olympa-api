package fr.olympa.api.auctions.gui;

import java.sql.SQLException;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.auctions.Auction;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Tax;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class MyAuctionsGUI extends PagedGUI<Auction> {

	private MoneyPlayerInterface player;
	private AuctionsManager manager;

	public MyAuctionsGUI(AuctionsManager manager, MoneyPlayerInterface player) {
		super("Mes articles", DyeColor.LIGHT_BLUE, manager.getAllAuctions().stream().filter(x -> x.player.equals(player.getInformation())).collect(Collectors.toList()), 5);
		this.manager = manager;
		this.player = player;
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		ItemStack item = object.getShownItem();
		ItemUtils.loreAdd(item, "§8---------------------");
		if (object.bought) {
			ItemUtils.loreAdd(item, "§6§lVendu !", "§e> §oClique pour récupérer tes gains");
		}else {
			ItemUtils.loreAdd(item, "§6§lEn attente...", "§c> §oClique pour annuler la vente");
		}
		return item;
	}

	@Override
	public void click(Auction existing, Player p) {
		if (existing.bought) {
			try {
				manager.removeAuction(existing);
				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as reçu %d (taxes retirées).", Tax.pay(player, existing.price));
			}catch (SQLException e) {
				e.printStackTrace();
				Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de ton paiement.");
			}
		}else {
			try {
				manager.removeAuction(existing);
				SpigotUtils.giveItems(p, existing.item);
				Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as annulé ta vente.");
			}catch (SQLException e) {
				e.printStackTrace();
				Prefix.ERROR.sendMessage(p, "Une erreur est survenue lors de la suppression de cette vente.");
			}
		}
		p.closeInventory();
	}
	
}
