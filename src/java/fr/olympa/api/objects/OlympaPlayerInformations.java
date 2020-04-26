package fr.olympa.api.objects;

import java.util.UUID;

public interface OlympaPlayerInformations {

	public long getId();

	public String getName();

	public UUID getUUID();

	@Override
	public boolean equals(Object obj);
	
}
