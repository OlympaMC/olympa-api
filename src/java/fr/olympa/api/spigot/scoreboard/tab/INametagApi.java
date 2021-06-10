package fr.olympa.api.spigot.scoreboard.tab;

import java.util.Collection;

import org.bukkit.event.EventPriority;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;

public interface INametagApi {

	void addNametagHandler(EventPriority priority, NametagHandler handler);

	void removeNametagHandler(NametagHandler handler);
	
	default void callNametagUpdate(OlympaPlayer player) {
		callNametagUpdate(player, AccountProviderAPI.getter().getAll(), true);
	}
	
	default void callNametagUpdate(OlympaPlayer player, boolean withDatas) {
		callNametagUpdate(player, AccountProviderAPI.getter().getAll(), withDatas);
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