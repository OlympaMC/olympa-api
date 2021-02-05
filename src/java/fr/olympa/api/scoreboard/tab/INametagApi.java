package fr.olympa.api.scoreboard.tab;

import org.bukkit.event.EventPriority;

import fr.olympa.api.player.OlympaPlayer;

public interface INametagApi {

	void addNametagHandler(EventPriority priority, NametagHandler handler);

	void callNametagUpdate(OlympaPlayer player);

	void callNametagUpdate(OlympaPlayer player, Iterable<? extends OlympaPlayer> toPlayers);

	public interface NametagHandler {
		void updateNameTag(Nametag nametag, OlympaPlayer player, OlympaPlayer to);
	}
}