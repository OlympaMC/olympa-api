package fr.olympa.api.scoreboard.tab;

import java.util.Collection;

import org.bukkit.event.EventPriority;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;

public interface INametagApi {

	void addNametagHandler(EventPriority priority, NametagHandler handler);

	void removeNametagHandler(NametagHandler handler);
	
	default void callNametagUpdate(OlympaPlayer player) {
		callNametagUpdate(player, AccountProvider.getAll(), true);
	}
	
	default void callNametagUpdate(OlympaPlayer player, boolean withDatas) {
		callNametagUpdate(player, AccountProvider.getAll(), withDatas);
	}

	default void callNametagUpdate(OlympaPlayer player, Collection<? extends OlympaPlayer> toPlayers) {
		callNametagUpdate(player, toPlayers, true);
	}
	
	void callNametagUpdate(OlympaPlayer player, Collection<? extends OlympaPlayer> toPlayers, boolean withDatas);

	@FunctionalInterface
	public interface NametagHandler {
		void updateNameTag(Nametag nametag, OlympaPlayer player, OlympaPlayer to);
		
		default boolean needsDatas() {
			return true;
		}
	}

}