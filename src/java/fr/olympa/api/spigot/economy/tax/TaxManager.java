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

	private double taxedMoney = 0;

	private double tax;
	private String taxFormatted;

	private OlympaStatement insertStatement;

	public TaxManager(Plugin plugin, OlympaSpigotPermission commandPermission, String tableName, double defaultTax) throws SQLException {
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
		Validate.isTrue(tax >= 0 && tax < 1, "La taxe doit être comprise entre 0 (inclus) et 1 (exclu)");
		this.tax = tax;
		this.taxFormatted = tax * 100 + "%";
		if (update) update();
	}

	public synchronized void update() throws SQLException {
		try (PreparedStatement statement = insertStatement.createStatement()) {
			statement.setLong(1, System.currentTimeMillis());
			statement.setDouble(2, tax);
			statement.setDouble(3, taxedMoney);
			insertStatement.executeUpdate(statement);
		}
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
