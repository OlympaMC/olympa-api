package fr.olympa.api.scoreboard;

import java.util.function.Function;

import fr.olympa.api.objects.OlympaPlayer;

public class DynamicLine extends ScoreboardLine {

	private Function<OlympaPlayer, String> value;

	public DynamicLine(Function<OlympaPlayer, String> value) {
		this(value, 0, 0);
	}

	public DynamicLine(Function<OlympaPlayer, String> value, int refresh, int length) {
		super(refresh, length);
		this.value = value;
	}

	public String getValue(OlympaPlayer player) {
		return value.apply(player);
	}

}
