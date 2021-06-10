package fr.olympa.api.spigot.auctions;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.economy.MoneyPlayerInterface;
import fr.olympa.api.spigot.economy.OlympaMoney;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;

public class Auction {

	private static final NumberFormat numberFormat = new DecimalFormat("00");

	public int id;
	public OlympaPlayerInformations player;
	public ItemStack item;
	public double price;
	public long expiration;
	public boolean bought;

	private BukkitTask task;
	private AuctionsManager manager;

	public Auction(AuctionsManager manager, int id, OlympaPlayerInformations player, ItemStack item, double price, long expiration, boolean bought) {
		this.manager = manager;
		this.id = id;
		this.player = player;
		this.item = item;
		this.price = price;
		this.expiration = expiration;
		this.bought = bought;

		if (!bought && !hasExpired()) {
			task = Bukkit.getScheduler().runTaskLater(manager.getPlugin(), () -> manager.auctionExpired(this), (expiration - System.currentTimeMillis()) / 50);
		}
	}

	public void expired() {
		if (task != null) task.cancel();
	}

	public synchronized void buy(Player p) {
		if (bought) throw new IllegalStateException("Auction already bought");
		MoneyPlayerInterface buyer = AccountProviderAPI.getter().get(p.getUniqueId());
		if (buyer.getGameMoney().withdraw(price)) {
			SpigotUtils.giveItems(p, item);
			Prefix.DEFAULT_GOOD.sendMessage(p, "L'achat s'est effectué. %s ont été retirés de ton compte !", OlympaMoney.format(price));
			try {
				manager.boughtAuction(this);
				Player seller = Bukkit.getPlayer(player.getUUID());
				if (seller != null && seller.isOnline()) Prefix.DEFAULT.sendMessage(seller, "§eUn joueur vient d'acheter ton item §7%s§e ! Retourne à l'Hôtel des Ventes pour récupérer tes gains.", OlympaMoney.format(price));
			}catch (SQLException e) {
				e.printStackTrace();
			}
		}else Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas assez d'argent pour acheter cet objet.");
	}

	public boolean hasExpired() {
		return expiration < System.currentTimeMillis();
	}
	
	public boolean isOngoing() {
		return !(hasExpired() || bought);
	}

	public String getTimeBeforeExpiration() {
		long time = expiration - System.currentTimeMillis();
		if (time <= 0) return "Expiré !";
		return Utils.durationToString(numberFormat, time);
	}

	public ItemStack getShownItem() {
		ItemStack item = this.item.clone();
		ItemUtils.loreAdd(item, "", "§8§m---------------------", "", "§ePrix: §6§l" + OlympaMoney.format(price), "§eProposé par §6§l" + player.getName(), hasExpired() ? "" : "§eExpire dans §6" + getTimeBeforeExpiration());
		return item;
	}

}
