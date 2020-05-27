package fr.olympa.api.auctions;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;

public class AuctionsGUI extends PagedGUI<Auction> {

	public AuctionsGUI(AuctionsManager manager) {
		super("Hôtel des Ventes", DyeColor.CYAN, manager.getAuctions(), p -> manager.openAuctionCreationGUI(p));
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		ItemStack item = object.item.clone();
		ItemUtils.loreAdd(item, "§8-------------", "§aClique pour acheter.", "", "§ePrix: §6§l" + object.price, "§eProposé par §6§l" + object.player.getName(), "§eExpire dans §6§l" + object.getTimeBeforeExpiration());
		return item;
	}

	@Override
	public void click(Auction existing, Player p) {

	}

}
