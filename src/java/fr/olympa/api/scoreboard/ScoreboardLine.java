package fr.olympa.api.scoreboard;

import org.apache.commons.lang.Validate;

public class ScoreboardLine {

	public String value;
	public int refresh = 0;
	public int length = 0;
	
	public ScoreboardLine(String value) {
		this(value, 0, 0);
	}

	public ScoreboardLine(String value, int refresh, int length) {
		Validate.notNull(value);
		this.value = value;
		this.refresh = refresh;
		this.length = length;
	}
	
	public String getValue(){
		return value;
	}
	
	public int getRefreshTime(){
		return refresh;
	}
	
	public int getMaxLength(){
		return length;
	}
	
}
