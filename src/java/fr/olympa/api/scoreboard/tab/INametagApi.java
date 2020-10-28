package fr.olympa.api.scoreboard.tab;

import java.util.Collection;

import org.bukkit.event.EventPriority;

import fr.olympa.api.player.OlympaPlayer;

public interface INametagApi {
	
	void addNametagHandler(EventPriority priority, NametagHandler handler);
	
	void callNametagUpdate(OlympaPlayer player);
	
	void callNametagUpdate(OlympaPlayer player, Collection<? extends OlympaPlayer> toPlayers);
	
	public static interface NametagHandler {
		public void updateNameTag(Nametag nametag, OlympaPlayer player, OlympaPlayer to);
	}
}