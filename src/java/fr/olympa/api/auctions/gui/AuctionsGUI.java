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
		super("Hôtel des Ventes", DyeColor.CYAN, manager.getOngoingAuctions(), 5);
		this.manager = manager;
		setBarItem(2, ItemUtils.item(Material.CHEST, "§a→ Mes objets"));
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		return ItemUtils.loreAdd(object.getShownItem(), "", "§aClique pour acheter !");
	}

	@Override
	public void click(Auction existing, Player p) {
		existing.buy(p);
	}

	@Override
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		if (barSlot == 2) {
			manager.openMyAuctionsGUI(p);
		}
		return true;
	}

}
