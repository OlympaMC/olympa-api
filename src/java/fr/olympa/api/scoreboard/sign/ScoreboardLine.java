package fr.olympa.api.scoreboard.sign;

import fr.olympa.api.player.OlympaPlayer;

public interface ScoreboardLine<T extends OlympaPlayer> {
	
	public String getValue(T player);
	
	public default void addScoreboard(Scoreboard<T> scoreboard) {}

	public default void removeScoreboard(Scoreboard<T> scoreboard) {}

}
