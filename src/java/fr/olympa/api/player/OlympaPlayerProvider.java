package fr.olympa.api.player;

import java.util.UUID;

public interface OlympaPlayerProvider {

	OlympaPlayer create(UUID uuid, String name, String ip);

}
