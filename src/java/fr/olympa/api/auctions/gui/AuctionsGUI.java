package fr.olympa.api.auctions.gui;

import java.sql.SQLException;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.auctions.Auction;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class AuctionsGUI<T extends MoneyPlayerInterface> extends PagedGUI<Auction> {

	private AuctionsManager manager;

	public AuctionsGUI(AuctionsManager manager) {
		super("Hôtel des Ventes", DyeColor.CYAN, manager.getOngoingAuctions(), 6);
		this.manager = manager;
		setBarItem(2, ItemUtils.item(Material.DIAMOND, "§aVendre un objet"));
		setBarItem(3, ItemUtils.item(Material.CHEST, "§aMes objets"));
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		ItemStack item = object.item.clone();
		ItemUtils.loreAdd(item, "§8-------------", "§aClique pour acheter.", "", "§ePrix: §6§l" + object.price, "§eProposé par §6§l" + object.player.getName(), "§eExpire dans §6§l" + object.getTimeBeforeExpiration());
		return item;
	}

	@Override
	public void click(Auction existing, Player p) {
		MoneyPlayerInterface player = AccountProvider.get(p.getUniqueId());
		if (existing.player.equals(player.getInformation())) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas acheter ton propre objet...");
			return;
		}
		if (player.getGameMoney().withdraw(existing.price)) {
			SpigotUtils.giveItems(p, existing.item);
			// TODO give money
			Prefix.DEFAULT_GOOD.sendMessage(p, "L'achat s'est effectué. %d ont été retirés de ton compte !", existing.price);
			try {
				manager.removeAuction(existing);
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		if (barSlot == 2) {
			manager.openAuctionCreationGUI(p);
		}else if (barSlot == 3) {

		}
		return true;
	}

}
