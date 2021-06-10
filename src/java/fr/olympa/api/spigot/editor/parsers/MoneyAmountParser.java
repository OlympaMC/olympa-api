package fr.olympa.api.spigot.editor.parsers;

import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import fr.olympa.api.spigot.economy.MoneyPlayerInterface;
import fr.olympa.api.utils.Prefix;

public class MoneyAmountParser extends NumberParser<Double> {

	private MoneyPlayerInterface player;
	private double min;
	private double max;

	public MoneyAmountParser(MoneyPlayerInterface player) {
		this(player, 1, Double.MAX_VALUE);
	}
	
	public MoneyAmountParser(MoneyPlayerInterface player, double min, double max) {
		super(Double.class, true, true);
		Validate.isTrue(min > 0, "La valeur minimale doit être strictement positive");
		Validate.isTrue(max > 0, "La valeur maximale doit être strictement positive");
		this.player = player;
		this.min = min;
		this.max = max;
	}

	@Override
	public Double parse(Player p, String msg) {
		Double value = super.parse(p, msg);
		if (value == null) return null;
		double money = value.doubleValue();
		
		if (money < min) {
			Prefix.BAD.sendMessage(p, "La valeur minimale est %f.", min);
			return null;
		}
		
		if (money > max) {
			Prefix.BAD.sendMessage(p, "La valeur maximale est %f.", max);
			return null;
		}
		
		if (!player.getGameMoney().has(money)) {
			Prefix.BAD.sendMessage(p, "Tu n'as pas autant d'argent !");
			return null;
		}
		return money;
	}

}
