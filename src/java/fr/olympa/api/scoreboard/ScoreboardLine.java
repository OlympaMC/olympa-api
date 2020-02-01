package fr.olympa.api.scoreboard;

import fr.olympa.api.objects.OlympaPlayer;

public abstract class ScoreboardLine<T extends OlympaPlayer> {

	public int refresh;
	public int length;

	public ScoreboardLine(int refresh, int length) {
		this.refresh = refresh;
		this.length = length;
	}
	
	public abstract String getValue(T player);
	
	public int getRefreshTime(){
		return refresh;
	}
	
	public int getMaxLength(){
		return length;
	}
	
}
