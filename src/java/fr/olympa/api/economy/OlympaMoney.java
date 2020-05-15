package fr.olympa.api.economy;

import fr.olympa.api.utils.Observable;

public class OlympaMoney extends Observable {

	private double money;

	public OlympaMoney(double base) {
		this.money = base;
	}

	public String getFormatted() {
		return money + "Ω";
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
		}else {
			return false;
		}
	}

}
