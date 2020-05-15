package fr.olympa.api.editor.parsers;

import org.bukkit.entity.Player;

import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.utils.Prefix;

public class MoneyAmountParser extends NumberParser<Double> {

	private MoneyPlayerInterface player;

	public MoneyAmountParser(MoneyPlayerInterface player) {
		super(Double.class, true, true);
		this.player = player;
	}

	@Override
	public Double parse(Player p, String msg) {
		Double value = super.parse(p, msg);
		if (value == null) return null;
		if (player.getGameMoney().has(value)) return value;
		Prefix.BAD.sendMessage(p, "Tu n'as pas autant d'argent !");
		return null;
	}

}
