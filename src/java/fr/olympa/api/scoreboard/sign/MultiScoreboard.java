package fr.olympa.api.scoreboard.sign;

import java.util.List;

import fr.olympa.api.player.OlympaPlayer;

public class MultiScoreboard<T extends OlympaPlayer> {

	List<Scoreboard<T>> scoreboards;
	Scoreboard<T> activeScoreboard;
	long secondsBetweenEachSb;

	public void switchSbTo(Scoreboard<T> sb) {
		// TODO Auto-generated method stub

	}

	public void switchSb() {
		// TODO Auto-generated method stub

	}

	public void addSb(Scoreboard<T> sb) {
		// TODO Auto-generated method stub

	}

	public boolean containsSb(Scoreboard<T> sb) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeSb(Scoreboard<T> sb) {
		// TODO Auto-generated method stub

	}
}
