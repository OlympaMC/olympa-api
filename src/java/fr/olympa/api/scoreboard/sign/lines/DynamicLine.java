package fr.olympa.api.scoreboard.sign.lines;

import java.util.function.Function;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Observable;

public class DynamicLine<T extends OlympaPlayer> extends ScoreboardLine<T> {

	private Function<T, String> value;

	public DynamicLine(Function<T, String> value) {
		this(value, null);
	}

	public DynamicLine(Function<T, String> value, Observable observable) {
		this.value = value;

		if (observable != null) observable.observe(this::updateGlobal);
	}

	@Override
	public String getValue(T player) {
		return value.apply(player);
	}

}
