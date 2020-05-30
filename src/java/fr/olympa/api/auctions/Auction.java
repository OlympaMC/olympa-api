package fr.olympa.api.auctions;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import fr.olympa.api.player.OlympaPlayerInformations;

public class Auction {

	private static final NumberFormat numberFormat = new DecimalFormat("00");

	public int id;
	public OlympaPlayerInformations player;
	public ItemStack item;
	public double price;
	public long expiration;

	private BukkitTask task;

	public Auction(AuctionsManager manager, int id, OlympaPlayerInformations player, ItemStack item, double price, long expiration) {
		this.id = id;
		this.player = player;
		this.item = item;
		this.price = price;
		this.expiration = expiration;

		task = new BukkitRunnable() {
			@Override
			public void run() {
				try {
					task = null;
					manager.removeAuction(Auction.this);
				}catch (SQLException e) {
					manager.getPlugin().getLogger().severe("Une erreur est survenue lors de la suppression de la vente " + id);
					e.printStackTrace();
				}
			}
		}.runTaskLater(manager.getPlugin(), (expiration - System.currentTimeMillis()) / 50);
	}

	public void deleted() {
		if (task != null) task.cancel();
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

}
