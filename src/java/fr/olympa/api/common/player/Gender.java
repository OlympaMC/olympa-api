package fr.olympa.api.common.player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum Gender {

	UNSPECIFIED("", "iel", "non spécifié"),
	FEMALE("e", "elle", "féminin", "fille", "femme", "meuf"),
	MALE("", "il", "masculin", "garçon", "homme", "mec"),
	UNBINARY("", "iel", "non-binaire");

	public static Gender get(int id) {
		return values()[id];
	}

	public static Gender get(String name) {
		return Arrays.stream(values()).filter(g -> g.names.length != 0 && Arrays.stream(g.names).anyMatch(n -> n.equalsIgnoreCase(name))).findFirst().orElse(null);
	}

	public static List<String> getNames() {
		return Arrays.stream(values()).filter(g -> g.names.length != 0).map(Gender::getName).collect(Collectors.toList());
	}

	final String tune;
	final String pronoun;

	final String[] names;

	private Gender(String tune, String pronoun, String... names) {
		this.tune = tune;
		this.pronoun = pronoun;
		this.names = names;
	}

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
