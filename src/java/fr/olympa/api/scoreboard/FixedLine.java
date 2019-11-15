package fr.olympa.api.scoreboard;

import fr.olympa.api.objects.OlympaPlayer;

public class FixedLine extends ScoreboardLine {

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
