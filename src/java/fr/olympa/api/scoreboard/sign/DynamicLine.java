package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Observable;

public class DynamicLine<T extends OlympaPlayer> implements ScoreboardLine<T> {

	private Function<T, String> value;

	private List<Scoreboard<T>> scoreboards = new ArrayList<>();

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

	@Override
	public void addScoreboard(Scoreboard<T> scoreboard) {
		scoreboards.add(scoreboard);
	}

	@Override
	public void removeScoreboard(Scoreboard<T> scoreboard) {
		scoreboards.remove(scoreboard);
	}

}
