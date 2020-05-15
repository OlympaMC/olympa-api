package fr.olympa.api.editor.parsers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.utils.Prefix;

public class PlayerParser implements TextParser<Player> {

	public static final PlayerParser PLAYER_PARSER = new PlayerParser();

	private PlayerParser() {}

	public Player parse(Player p, String msg) {
		Player target = Bukkit.getPlayer(msg);
		if (target == null) Prefix.BAD.sendMessage(p, "Ce joueur n'est pas connecté.");
		return target;
	}

}
