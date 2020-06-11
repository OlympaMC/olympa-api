package fr.olympa.api.economy.tax;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.plugin.Plugin;

import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.sql.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;

public class TaxManager {

	private double taxedMoney = 0;

	private double tax;
	private String taxFormatted;

	private OlympaStatement insertStatement;

	public TaxManager(Plugin plugin, OlympaPermission commandPermission, String tableName, double defaultTax) throws SQLException {
		tableName = "`" + tableName + "`";
		
		OlympaCore.getInstance().getDatabase().createStatement().executeUpdate("CREATE TABLE IF NOT EXISTS " + tableName + " (" +
				"  `date` BIGINT UNSIGNED NOT NULL," +
				"  `tax` DOUBLE UNSIGNED NOT NULL," +
				"  `total_taxed` DOUBLE UNSIGNED NOT NULL," +
				"  PRIMARY KEY (`date`))");
		
		double tax = defaultTax;
		ResultSet resultSet = OlympaCore.getInstance().getDatabase().createStatement().executeQuery("SELECT `tax`, `total_taxed` FROM " + tableName + " ORDER BY `date` DESC LIMIT 1");
		if (resultSet.next()) {
			tax = resultSet.getDouble("tax");
			taxedMoney = resultSet.getDouble("total_taxed");
		}
		setTax(tax, false);
		
		insertStatement = new OlympaStatement("INSERT INTO " + tableName + " (`date`, `tax`, `total_taxed`) VALUES(?, ?, ?)");

		new TaxCommand(plugin, commandPermission, this).register();
	}

	public void setTax(double tax, boolean update) throws SQLException {
		this.tax = tax;
		this.taxFormatted = tax * 100 + "%";
		if (update) update();
	}

	public synchronized void update() throws SQLException {
		PreparedStatement statement = insertStatement.getStatement();
		statement.setLong(1, System.currentTimeMillis());
		statement.setDouble(2, tax);
		statement.setDouble(3, taxedMoney);
		statement.executeUpdate();
	}

	public String getTax() {
		return taxFormatted;
	}

	public double pay(MoneyPlayerInterface player, double amount) {
		double taxed = amount * tax;
		taxedMoney += taxed;
		amount -= taxed;
		player.getGameMoney().give(amount);
		return amount;
	}

	public double getTotalTaxedMoney() {
		return taxedMoney;
	}

}
