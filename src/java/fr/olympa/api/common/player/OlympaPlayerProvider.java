package fr.olympa.api.common.player;

import java.util.UUID;

@FunctionalInterface
public interface OlympaPlayerProvider {

	OlympaPlayer create(UUID uuid, String name, String ip);

}
