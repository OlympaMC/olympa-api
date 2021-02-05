package fr.olympa.api.vanish;

import java.util.stream.Stream;

import org.bukkit.entity.Player;

import fr.olympa.api.player.OlympaPlayer;

public interface IVanishApi {

	void disable(OlympaPlayer olympaPlayer, boolean showMessage);

	void enable(OlympaPlayer olympaPlayer, boolean showMessage);

	void addVanishMetadata(Player player);

	Stream<? extends Player> getVanished();

	boolean isVanished(Player player);

	void removeVanishMetadata(Player player);

}