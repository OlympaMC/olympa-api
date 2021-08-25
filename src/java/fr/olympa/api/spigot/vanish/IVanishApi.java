package fr.olympa.api.spigot.vanish;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.entity.Player;

import fr.olympa.api.common.player.OlympaPlayer;

public interface IVanishApi {

	void disable(OlympaPlayer olympaPlayer, boolean showMessage);

	void enable(OlympaPlayer olympaPlayer, boolean showMessage);

	void addVanishMetadata(Player player);

	Stream<? extends Player> getVanished();

	boolean isVanished(Player player);

	void removeVanishMetadata(Player player);

	/**
	 * @param name
	 * @param biConsumer key = olympaPlayer value = willBecomeVanished
	 * @return false if other handler has already register with same name. Parameters override this old entry
	 */
	boolean registerHandler(String name, TriConsumer<Player, OlympaPlayer, Boolean> biConsumer);

	boolean unRegisterHandler(String name);

}