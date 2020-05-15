package fr.olympa.api.scoreboard.tab;

import java.util.Collection;

import org.bukkit.entity.Player;

import fr.olympa.api.player.OlympaPlayer;

public interface INametagApi {

	void clearNametag(Player player);

	void clearNametag(String player);

	FakeTeam getFakeTeam(Player player);

	Nametag getNametag(Player player);

	void reset();

	void reset(String player);

	void sendTeams(Player player);

	void setNametag(OlympaPlayer olympaPlayer);

	void setNametag(String player, String prefix, String suffix);

	void setPrefix(String player, String prefix);

	void setSuffix(String player, String suffix);

	void updateFakeNameTag(Player player, Nametag nametag, Collection<? extends Player> toPlayers);

	void updateFakeNameTag(String player, Nametag nametag, Collection<? extends Player> toPlayers);
}