package fr.olympa.api.spigot.economy.tax;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.lang.Validate;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.spigot.economy.MoneyPlayerInterface;
import fr.olympa.core.spigot.OlympaCore;

public class TaxManager {

	private long lastUpdate = 0;
	private double taxedMoney = 0;
	private double tax;
	
	private String taxFormatted;

	private OlympaStatement insertStatement;
	private OlympaStatement updateStatement;

	public TaxManager(Plugin plugin, OlympaSpigotPermission commandPermission, String tableName, double defaultTax) throws SQLException {
		tableName = "`" + tableName + "`";
		
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `date` BIGINT UNSIGNED NOT NULL," +
				"  `tax` DOUBLE UNSIGNED NOT NULL," +
				"  `total_taxed` DOUBLE UNSIGNED DEFAULT 0," +
				"  PRIMARY KEY (`date`))");
		
		double tax = defaultTax;
		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT * FROM " + tableName + " ORDER BY `date` DESC LIMIT 1");
		if (resultSet.next()) {
			lastUpdate = resultSet.getLong("date");
			tax = resultSet.getDouble("tax");
			taxedMoney = resultSet.getDouble("total_taxed");
		}
		setTax(tax, false);
		
		insertStatement = new OlympaStatement("INSERT INTO " + tableName + " (`date`, `tax`) VALUES(?, ?)");
		updateStatement = new OlympaStatement("UPDATE " + tableName + " SET `total_taxed` = ? WHERE `date` = ?");

		new TaxCommand(plugin, commandPermission, this).register();
	}

	public void setTax(double tax, boolean update) throws SQLException {
		Validate.isTrue(tax >= 0 && tax < 1, "La taxe doit être comprise entre 0 (inclus) et 1 (exclu)");
		this.tax = tax;
		this.taxFormatted = tax * 100 + "%";
		if (update) {
			try (PreparedStatement statement = insertStatement.createStatement()) {
				statement.setLong(1, System.currentTimeMillis());
				statement.setDouble(2, tax);
				insertStatement.executeUpdate(statement);
			}
		}
	}

	private synchronized void update() throws SQLException {
		try (PreparedStatement statement = updateStatement.createStatement()) {
			statement.setDouble(1, taxedMoney);
			statement.setLong(2, System.currentTimeMillis());
			updateStatement.executeUpdate(statement);
		}
	}

	public String getTax() {
		return taxFormatted;
	}

	public synchronized double pay(MoneyPlayerInterface player, double amount) {
		double taxed = amount * tax;
		taxedMoney += taxed;
		amount -= taxed;
		player.getGameMoney().give(amount);
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> {
			try {
				update();
			}catch (SQLException e) {
				e.printStackTrace();
			}
		});
		return amount;
	}

	public double getTotalTaxedMoney() {
		return taxedMoney;
	}

}
