package fr.olympa.api.scoreboard.sign.lines;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.sign.Scoreboard;

public abstract class ScoreboardLine<T extends OlympaPlayer> {

	private Map<Scoreboard<T>, String> scoreboards = new HashMap<>();
	
	public abstract String getValue(T player);
	
	public void addScoreboard(Scoreboard<T> scoreboard) {
		scoreboards.put(scoreboard, null);
	}

	public void removeScoreboard(Scoreboard<T> scoreboard) {
		scoreboards.remove(scoreboard);
	}

	public void updateGlobal() {
		for (Entry<Scoreboard<T>, String> scoreboard : scoreboards.entrySet()) {
			if (!scoreboard.getValue().equals(getValue(scoreboard.getKey().p))) scoreboard.getKey().needsUpdate();
		}
	}

	public void updatePlayer(T player) {
		for (Entry<Scoreboard<T>, String> scoreboard : scoreboards.entrySet()) {
			if (scoreboard.getKey() == player) {
				if (!scoreboard.getValue().equals(getValue(player))) scoreboard.getKey().needsUpdate();
				return;
			}
		}
	}

}
