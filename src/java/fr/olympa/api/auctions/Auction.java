package fr.olympa.api.auctions;

import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;

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
		MoneyPlayerInterface buyer = AccountProvider.get(p.getUniqueId());
		if (player.equals(buyer.getInformation())) {
			Prefix.DEFAULT_BAD.sendMessage(p, "Tu ne peux pas acheter ton propre objet...");
			return;
		}
		if (buyer.getGameMoney().withdraw(price)) {
			SpigotUtils.giveItems(p, item);
			Prefix.DEFAULT_GOOD.sendMessage(p, "L'achat s'est effectué. %d ont été retirés de ton compte !", price);
			this.bought = true;
			manager.auctionExpired(this);
		}else Prefix.DEFAULT_BAD.sendMessage(p, "Tu n'as pas assez d'argent pour acheter cet objet.");
	}

	public boolean hasExpired() {
		return expiration < System.currentTimeMillis();
	}

	public String getTimeBeforeExpiration() {
		long time = expiration - System.currentTimeMillis();
		if (time <= 0) return "Expiré !";
		StringBuilder sb = new StringBuilder();

		long days = time / 86_400_000;
		if (days != 0) sb.append(numberFormat.format(days)).append('J');
		time -= days * 86_400_000;

		long hours = time / 3_600_000;
		if (sb.length() != 0) sb.append(' ');
		sb.append(numberFormat.format(hours)).append("H ");
		time -= hours * 3_600_000;

		long minutes = time / 60_000;
		sb.append(numberFormat.format(minutes)).append("M ");
		time -= minutes * 60_000;

		long seconds = time / 1_000;
		sb.append(numberFormat.format(seconds)).append("S");

		return sb.toString();
	}

	public ItemStack getShownItem() {
		ItemStack item = this.item.clone();
		ItemUtils.loreAdd(item, "", "§8---------------------", "§aClique pour acheter.", "", "§ePrix: §6§l" + price, "§eProposé par §6§l" + player.getName(), "§eExpire dans §6" + getTimeBeforeExpiration());
		return item;
	}

}
