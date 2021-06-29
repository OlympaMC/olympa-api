package fr.olympa.api.common.provider;

import java.util.UUID;

import fr.olympa.api.common.player.OlympaPlayerInformations;

public class OlympaPlayerInformationsAPI implements OlympaPlayerInformations {

	protected long id;
	private String name;
	private UUID uuid;

	public OlympaPlayerInformationsAPI(long id, String name, UUID uuid) {
		this.id = id;
		this.name = name;
		this.uuid = uuid;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public UUID getUUID() {
		return uuid;
	}

}
