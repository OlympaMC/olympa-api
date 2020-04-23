package fr.olympa.api.provider;

import java.util.UUID;

import fr.olympa.api.objects.OlympaPlayerInformations;

public class OlympaPlayerInformationsObject implements OlympaPlayerInformations {

	private long id;
	private String name;
	private UUID uuid;
	
	public OlympaPlayerInformationsObject(long id, String name, UUID uuid) {
		this.id = id;
		this.name = name;
		this.uuid = uuid;
	}

	@Override
	public long getID() {
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