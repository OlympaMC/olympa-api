package fr.olympa.api.scoreboard.sign;

import fr.olympa.api.player.OlympaPlayer;

public class FixedLine extends ScoreboardLine<OlympaPlayer> {

	public static final FixedLine EMPTY_LINE = new FixedLine("");

	private String value;

	public FixedLine(String value) {
		this(value, 0);
	}

	public FixedLine(String value, int length) {
		super(0, length);
		this.value = value;
	}

	public String getValue(OlympaPlayer player) {
		return value;
	}

}
