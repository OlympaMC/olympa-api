package fr.olympa.api.objects;

import java.util.Arrays;

public enum Gender {

	MALE(0, "", "il", "garçon"),
	FEMALE(1, "e", "elle", "fille");

	public static Gender get(int int1) {
		return Arrays.stream(values()).filter(i -> int1 == i.getId()).findFirst().orElse(null);
	}

	int id;
	String turning;
	String pronoun;

	String name;

	private Gender(int id, String turning, String pronoun, String name) {
		this.id = id;
		this.turning = turning;
		this.pronoun = pronoun;
		this.name = name;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getPronoun() {
		return this.pronoun;
	}

	public String getTurning() {
		return this.turning;
	}

	public void setId(int id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPronoun(String pronoun) {
		this.pronoun = pronoun;
	}

	public void setTurning(String turning) {
		this.turning = turning;
	}
}
