package fr.olympa.api.common.player;

import java.util.UUID;

public interface OlympaPlayerInformations {
	
	long getId();
	
	String getName();
	
	UUID getUUID();
	
	@Override
	boolean equals(Object obj);

}
