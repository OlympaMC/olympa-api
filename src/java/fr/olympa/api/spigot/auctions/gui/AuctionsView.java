package fr.olympa.api.spigot.auctions.gui;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.auctions.Auction;
import fr.olympa.api.spigot.auctions.AuctionsManager;
import fr.olympa.api.spigot.economy.MoneyPlayerInterface;
import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.gui.templates.ConfirmGUI;
import fr.olympa.api.spigot.gui.templates.PagedView;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class AuctionsView<T extends MoneyPlayerInterface> extends PagedView<Auction> {

	public static DyeColor[] PRETTY_COLORS = { DyeColor.BLUE, DyeColor.LIME, DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.LIGHT_BLUE, DyeColor.MAGENTA, DyeColor.PINK, DyeColor.RED };
	
	private AuctionsManager manager;
	private T player;
	
	private Random random = new Random();
	private BukkitTask colorTask;
	
	public AuctionsView(AuctionsManager manager, T player) {
		super(DyeColor.CYAN, manager.getOngoingAuctions());
		this.manager = manager;
		this.player = player;
	}
	
	@Override
	public void init() {
		super.init();
		List<Auction> myAuctions = manager.getAllAuctions().stream().filter(x -> x.player.equals(player.getInformation())).collect(Collectors.toList());
		long ongoing = myAuctions.stream().filter(Auction::isOngoing).count();
		right.setItem(2, ItemUtils.item(Material.CHEST, "§a→ Mes objets", 
				"§8> §7" + Utils.withOrWithoutS(ongoing, "enchère") + " en cours",
				"§8> §7" + Utils.withOrWithoutS(myAuctions.size() - ongoing, "enchère") + " terminées"
				));
		
		colorTask = Bukkit.getScheduler().runTaskTimer(OlympaCore.getInstance(), () -> setSeparatorItems(PRETTY_COLORS[random.nextInt(PRETTY_COLORS.length)]), 20, 20);
	}

	@Override
	public ItemStack getItemStack(Auction object) {
		return ItemUtils.loreAdd(object.getShownItem(), "", "§aClique pour acheter !");
	}

	@Override
	public void click(Auction existing, Player p, ClickType click) {
		if (existing.player.equals(AccountProviderAPI.getter().get(p.getUniqueId()).getInformation())) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas acheter ton propre objet...");
		}else {
			new ConfirmGUI(() -> {
				if (existing.hasExpired() || existing.bought) {
					Prefix.BAD.sendMessage(p, "Trop tard... cette vente n'est plus disponible.");
				}else existing.buy(p);
			}, () -> p.closeInventory(), "§7§oVeux-tu acheter cet objet ?").create(p);
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
		super.onClose(p);
		colorTask.cancel();
		return true;
	}
	
	public OlympaGUI toGUI() {
		return super.toGUI("Hôtel des Ventes", 5);
	}

}
