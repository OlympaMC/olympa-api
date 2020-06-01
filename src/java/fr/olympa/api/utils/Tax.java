package fr.olympa.api.utils;

import fr.olympa.api.economy.MoneyPlayerInterface;

public class Tax {

	private static double taxedMoney = 0;

	private static double tax;
	private static String taxFormatted;

	public static void setTax(double tax) {
		Tax.tax = tax;
		taxFormatted = tax * 100 + "%%";
	}

	public static String getTax() {
		return taxFormatted;
	}

	public static double pay(MoneyPlayerInterface player, double amount) {
		double taxed = amount * tax;
		taxedMoney += taxed;
		amount -= taxed;
		player.getGameMoney().give(amount);
		return amount;
	}

	public static double getTotalTaxedMoney() {
		return taxedMoney;
	}

}
