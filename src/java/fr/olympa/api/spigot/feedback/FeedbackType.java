package fr.olympa.api.spigot.feedback;

import java.util.Arrays;

public enum FeedbackType {

	BUG(
			0,
			"Écris dans le chat une description du bug que tu rencontres."),
	SUGGESTION(
			1,
			"Écris dans le chat ce que tu souhaites suggérer au staff."),
	AVIS(
			2,
			"Écris dans le chat l'avis que tu portes sur une fonctionnalité du serveur.");
	
	private int id;
	private String descriptionMessage;
	
	private FeedbackType(int id, String descriptionMessage) {
		this.id = id;
		this.descriptionMessage = descriptionMessage;
	}
	
	public int getId() {
		return id;
	}
	
	public String getDescriptionMessage() {
		return descriptionMessage;
	}
	
	public static FeedbackType fromId(int id) {
		return Arrays.stream(values()).filter(type -> type.id == id).findAny().get();
	}
	
}
