package fr.olympa.api.player;

import java.util.UUID;

import fr.olympa.api.clans.ClanPlayerData;

public interface OlympaPlayerInformations {
	
	long getId();
	
	String getName();

	ClanPlayerData getClanData();
	
	UUID getUUID();
	
	@Override
	boolean equals(Object obj);

}
