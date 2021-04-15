package fr.olympa.api.trades;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import fr.olympa.api.player.OlympaPlayerInformations;

public class UniqueTradeHistory {

	private OlympaPlayerInformations p1;
	private OlympaPlayerInformations p2;

	private List<ItemStack> recievedItemsP1;
	private List<ItemStack> recievedItemsP2;

	private double recievedMoneyP1;
	private double recievedMoneyP2;
}
