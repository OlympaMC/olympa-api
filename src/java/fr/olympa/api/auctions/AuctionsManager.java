package fr.olympa.api.auctions;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.auctions.gui.AuctionsGUI;
import fr.olympa.api.auctions.gui.CreateAuctionGUI;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.api.utils.observable.ObservableList;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class AuctionsManager {

	private final String tableName;
	private final Plugin plugin;
	private final Map<Integer, Auction> auctions = new HashMap<>();
	private final ObservableList<Auction> ongoingAuctions = new ObservableList<>(new ArrayList<>());

	private OlympaStatement createAuctionStatement;
	private OlympaStatement removeAuctionStatement;

	public AuctionsManager(Plugin plugin, String table) throws SQLException, ClassNotFoundException, IOException {
		this.plugin = plugin;
		this.tableName = "`" + table + "`";
		
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT," +
				"  `player_id` BIGINT NOT NULL," +
				"  `item` VARBINARY(8000) NOT NULL," +
				"  `price` DOUBLE NOT NULL," +
				"  `expiration` BIGINT NOT NULL,"
				+
				"  PRIMARY KEY (`id`))");
		
		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName);
		while (resultSet.next()) {
			addAuction(new Auction(this, resultSet.getInt("id"), AccountProvider.getPlayerInformations(resultSet.getLong("player_id")), SpigotUtils.<ItemStack>deserialize(resultSet.getBytes("item")), resultSet.getDouble("price"), resultSet.getLong("expiration")));
		}

		createAuctionStatement = new OlympaStatement("INSERT INTO " + tableName + " (``player_id`, `item`, `price`, `expiration`) VALUES (?, ?, ?, ?)", true);
		removeAuctionStatement = new OlympaStatement("DELETE FROM " + tableName + " WHERE (`id` = ?)");
	}

	public synchronized void createAuction(OlympaPlayerInformations player, ItemStack item, double price, long expiration) throws SQLException, IOException {
		PreparedStatement statement = createAuctionStatement.getStatement();
		statement.setLong(1, player.getId());
		statement.setBytes(2, SpigotUtils.serialize(item));
		statement.setDouble(3, price);
		statement.setLong(4, expiration);
		statement.executeUpdate();
		ResultSet resultSet = statement.getGeneratedKeys();
		resultSet.next();
		addAuction(new Auction(this, resultSet.getInt(1), player, item, price, expiration));
	}

	public synchronized void removeAuction(Auction auction) throws SQLException {
		PreparedStatement statement = removeAuctionStatement.getStatement();
		statement.setInt(1, auction.id);
		statement.executeUpdate();
		auctions.remove(auction.id);
		if (!auction.hasExpired()) ongoingAuctions.remove(auction);
		auction.deleted();
	}

	public void auctionExpired(Auction auction) {
		ongoingAuctions.remove(auction);
	}

	private void addAuction(Auction auction) {
		auctions.put(auction.id, auction);
		if (!auction.hasExpired()) ongoingAuctions.add(auction);
	}

	public ObservableList<Auction> getOngoingAuctions() {
		return ongoingAuctions;
	}

	public Collection<Auction> getAllAuctions() {
		return auctions.values();
	}

	public Plugin getPlugin() {
		return plugin;
	}

	public int getPriceMax() {
		return 100_00_000;
	}

	public int getDemidaysMin() {
		return 2;
	}

	public int getDemidaysMax() {
		return 10;
	}

	public <T extends MoneyPlayerInterface> void openAuctionsGUI(Player p) {
		new AuctionsGUI<T>(this).create(p);
	}

	public void openAuctionCreationGUI(Player p) {
		new CreateAuctionGUI(this).create(p);
	}

}
