package fr.olympa.api.scoreboard.sign;

import fr.olympa.api.player.OlympaPlayer;

public class FixedLine<T extends OlympaPlayer> implements ScoreboardLine<T> {

	@SuppressWarnings ("rawtypes")
	public static final FixedLine EMPTY_LINE = new FixedLine<>("");

	private String value;

	public FixedLine(String value) {
		this.value = value;
	}

	public String getValue(OlympaPlayer player) {
		return value;
	}

}
