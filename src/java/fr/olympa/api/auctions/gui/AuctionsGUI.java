package fr.olympa.api.auctions.gui;

import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.auctions.Auction;
import fr.olympa.api.auctions.AuctionsManager;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.gui.templates.ConfirmGUI;
import fr.olympa.api.gui.templates.PagedGUI;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class AuctionsGUI<T extends MoneyPlayerInterface> extends PagedGUI<Auction> {

	public static DyeColor[] PRETTY_COLORS = { DyeColor.BLUE, DyeColor.LIME, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIGHT_BLUE, DyeColor.MAGENTA, DyeColor.PINK, DyeColor.RED };
	
	private AuctionsManager manager;
	
	private Random random = new Random();
	private BukkitTask colorTask;
	
	public AuctionsGUI(AuctionsManager manager) {
		super("Hôtel des Ventes", DyeColor.CYAN, manager.getOngoingAuctions(), 5);
		this.manager = manager;
		setBarItem(2, ItemUtils.item(Material.CHEST, "§a→ Mes objets"));
		
		colorTask = Bukkit.getScheduler().runTaskTimer(OlympaCore.getInstance(), () -> setSeparatorItems(PRETTY_COLORS[random.nextInt(PRETTY_COLORS.length)]), 20, 20);
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		return ItemUtils.loreAdd(object.getShownItem(), "", "§aClique pour acheter !");
	}

	@Override
	public void click(Auction existing, Player p) {
		if (existing.player.equals(AccountProvider.get(p.getUniqueId()).getInformation())) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas acheter ton propre objet...");
		}else {
			new ConfirmGUI(() -> {
				if (existing.hasExpired() || existing.bought) {
					Prefix.BAD.sendMessage(p, "Trop tard... cette vente n'est plus disponible.");
				}else existing.buy(p);
			}, () -> p.closeInventory(), "Voulez-vous acheter cet objet ?");
		}
	}

	@Override
	protected boolean onBarItemClick(Player p, ItemStack current, int barSlot, ClickType click) {
		if (barSlot == 2) {
			manager.openMyAuctionsGUI(p);
		}
		return true;
	}
	
	@Override
	public boolean onClose(Player p) {
		colorTask.cancel();
		return true;
	}

}
