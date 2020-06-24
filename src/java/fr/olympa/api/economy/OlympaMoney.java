package fr.olympa.api.economy;

import java.text.DecimalFormat;

import fr.olympa.api.utils.observable.AbstractObservable;

public class OlympaMoney extends AbstractObservable {

	private double money;

	public OlympaMoney(double base) {
		money = base;
	}

	public String getFormatted() {
		DecimalFormat df = new DecimalFormat("##.##");
		return df.format(money) + "Ω";
	}

	public double get() {
		return money;
	}

	public boolean has(double money) {
		return this.money >= money;
	}

	public void set(double money) {
		this.money = money;
		update();
	}

	public void give(double money) {
		this.money += money;
		update();
	}

	public boolean withdraw(double money) {
		if (this.money >= money) {
			this.money -= money;
			update();
			return true;
		} else
			return false;
	}

}
