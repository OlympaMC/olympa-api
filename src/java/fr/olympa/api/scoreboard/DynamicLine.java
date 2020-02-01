package fr.olympa.api.scoreboard;

import java.util.function.Function;

import fr.olympa.api.objects.OlympaPlayer;

public class DynamicLine<T extends OlympaPlayer> extends ScoreboardLine<T> {

	private Function<T, String> value;

	public DynamicLine(Function<T, String> value) {
		this(value, 0, 0);
	}

	public DynamicLine(Function<T, String> value, int refresh, int length) {
		super(refresh, length);
		this.value = value;
	}

	public String getValue(T player) {
		return value.apply(player);
	}

}
