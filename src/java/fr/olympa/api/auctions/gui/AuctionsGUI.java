package fr.olympa.api.auctions.gui;

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
		return object.getShownItem();
	}

	@Override
	public void click(Auction existing, Player p) {
		existing.buy(p);
	}

	@Override
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		if (barSlot == 2) {
			manager.openAuctionCreationGUI(p);
		}else if (barSlot == 3) {
			manager.openMyAuctionsGUI(p);
		}
		return true;
	}

}
