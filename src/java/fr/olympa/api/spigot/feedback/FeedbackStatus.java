package fr.olympa.api.spigot.feedback;

import java.util.Arrays;

public enum FeedbackStatus {
	
	CRITICAL(0), IMPORTANT(1), MEDIUM(2), MINOR(3);
	
	private int id;

	private FeedbackStatus(int id) {
		this.id = id;
	}
	
	public int getId() {
		return id;
	}
	
	public static FeedbackStatus fromId(int id) {
		return Arrays.stream(values()).filter(type -> type.id == id).findAny().get();
	}

}
