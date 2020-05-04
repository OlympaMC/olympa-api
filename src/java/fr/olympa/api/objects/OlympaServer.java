package fr.olympa.api.objects;

import java.util.Arrays;

import fr.olympa.api.utils.Utils;

public enum OlympaServer {

	ALL,
	AUTH,
	LOBBY,
	DEV,
	BUILDEUR,
	ZTA,
	CREATIF,
	LG,
	PVPFAC;

	public static OlympaServer get(String name) {
		return Arrays.stream(OlympaServer.values()).filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	public String getName() {
		return toString().toLowerCase();
	}

	public String getNameCaps() {
		return Utils.capitalize(toString());
	}

}
