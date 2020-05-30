package fr.olympa.api.auctions.gui;

import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.auctions.Auction;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.gui.templates.PagedGUI;

public class MyAuctionsGUI extends PagedGUI<Auction> {

	public MyAuctionsGUI(AuctionsManager manager, MoneyPlayerInterface player) {
		super("Mes articles", DyeColor.LIGHT_BLUE, manager.getAllAuctions().stream().filter(x -> x.player.equals(player.getInformation())).collect(Collectors.toList()), 5);
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		return null;
	}

	@Override
	public void click(Auction existing, Player p) {}
	
}
