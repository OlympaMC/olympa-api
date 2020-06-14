package fr.olympa.api.server;

import java.util.Arrays;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.utils.Utils;

public enum OlympaServer {

	ALL(null),
	AUTH(null),
	LOBBY(null),
	DEV(OlympaGroup.DEV),
	BUILDEUR(OlympaGroup.BUILDER),
	ZTA,
	CREATIF,
	LG,
	PVPFAC;

	private OlympaGroup minAllowed;

	private OlympaServer() {
		this(OlympaGroup.PLAYER);
	}

	private OlympaServer(OlympaGroup minAllowed) {
		this.minAllowed = minAllowed;
	}

	public OlympaGroup getMinGroupAllowed() {
		return minAllowed;
	}

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
