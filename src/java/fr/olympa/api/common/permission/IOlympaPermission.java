package fr.olympa.api.common.permission;

import java.util.Map;
import java.util.UUID;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;

public interface IOlympaPermission {

	String getName();

	boolean hasPermission(UUID uniqueId);

	boolean hasPermission(OlympaPlayer olympaPlayer);

	boolean hasPermission(Map<OlympaGroup, Long> groups);

	boolean hasPermission(OlympaGroup group);

}