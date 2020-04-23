package fr.olympa.api.objects;

import java.util.Arrays;

import fr.olympa.api.utils.Utils;

public enum OlympaServer {

	ALL(ProtocolAPI.V1_8),
	AUTH(ProtocolAPI.V1_8),
	LOBBY(ProtocolAPI.V1_8),
	DEV(ProtocolAPI.V1_8),
	BUILDEUR(ProtocolAPI.V1_8),
	ZTA(ProtocolAPI.V1_9),
	CREATIF(ProtocolAPI.V1_8),
	LG(ProtocolAPI.V1_8),
	PVPFAC(ProtocolAPI.V1_9);

	public static OlympaServer get(String name) {
		return Arrays.stream(OlympaServer.values()).filter(s -> s.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
	}

	final ProtocolAPI protocolApi;

	private OlympaServer(ProtocolAPI protocolApi) {
		this.protocolApi = protocolApi;
	}

	public String getName() {
		return toString().toLowerCase();
	}

	public String getNameCaps() {
		return Utils.capitalize(toString());
	}
}
