package fr.olympa.api.player;

import java.util.UUID;

@FunctionalInterface
public interface OlympaPlayerProvider {

	OlympaPlayer create(UUID uuid, String name, String ip);

}
