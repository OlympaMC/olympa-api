package fr.olympa.api.scoreboard;

import fr.olympa.api.objects.OlympaPlayer;

public abstract class ScoreboardLine {

	public int refresh;
	public int length;

	public ScoreboardLine(int refresh, int length) {
		this.refresh = refresh;
		this.length = length;
	}
	
	public abstract String getValue(OlympaPlayer player);
	
	public int getRefreshTime(){
		return refresh;
	}
	
	public int getMaxLength(){
		return length;
	}
	
}
