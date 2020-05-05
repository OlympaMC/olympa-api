package fr.olympa.api.objects;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Gender {

	NO_SPECIFED(0, "", "il"),
	FEMALE(1, "e", "elle", "féminin", "fille", "femme", "meuf"),
	MALE(2, "", "il", "masculin", "garçon", "homme", "mec");

	public static Gender get(int int1) {
		return Arrays.stream(values()).filter(i -> int1 == i.getId()).findFirst().orElse(null);
	}

	public static Gender get(String name) {
		return Arrays.stream(values()).filter(g -> g.names != null && Arrays.stream(g.names).anyMatch(n -> n.equalsIgnoreCase(name))).findFirst().orElse(null);
	}

	public static List<String> getNames() {
		return Arrays.stream(values()).map(Gender::getName).collect(Collectors.toList());
	}

	final int id;
	final String tune;
	final String pronoun;

	final String[] names;

	private Gender(int id, String tune, String pronoun, String... names) {
		this.id = id;
		this.tune = tune;
		this.pronoun = pronoun;
		this.names = names;
	}

	public int getId() {
		return id;
	}

	/**
	 * @throws NullPointerException Si le genre est NO_SPECIFED
	 */
	public String getName() {
		return names[0];
	}

	public String getPronoun() {
		return pronoun;
	}

	public String getTurne() {
		return tune;
	}
}
