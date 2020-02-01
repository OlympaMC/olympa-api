package fr.olympa.api.objects;

public class OlympaMoney {

	private double money;

	public OlympaMoney(double money) {
		this.money = money;
	}

	public double get() {
		return money;
	}

	public String getFormatted() {
		return money + "Ω";
	}

	public void give(double money) {
		this.money += money;
	}

	public boolean has(double money) {
		return this.money >= money;
	}

	public boolean withdraw(double money) {
		if (this.money >= money) {
			this.money -= money;
			return true;
		}else {
			return false;
		}
	}

}
