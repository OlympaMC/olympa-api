package fr.olympa.api.scoreboard.sign.lines;

import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.sign.Scoreboard;

public abstract class ScoreboardLine<T extends OlympaPlayer> {

	private List<Scoreboard<T>> scoreboards = new ArrayList<>();
	
	public abstract String getValue(T player);
	
	public void addScoreboard(Scoreboard<T> scoreboard) {
		scoreboards.add(scoreboard);
	}

	public void removeScoreboard(Scoreboard<T> scoreboard) {
		scoreboards.remove(scoreboard);
	}

	public void updateGlobal() {
		scoreboards.forEach(Scoreboard::needsUpdate);
	}

	public void updatePlayer(T player) {
		for (Scoreboard<T> scoreboard : scoreboards) {
			if (scoreboard.p == player) {
				scoreboard.needsUpdate();
				return;
			}
		}
	}

}
