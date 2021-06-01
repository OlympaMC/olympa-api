package fr.olympa.api.spigot.editor.parsers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;

public class OlympaPlayerParser<T extends OlympaPlayer> implements TextParser<T> {

	private static final OlympaPlayerParser<?> OLYMPA_PLAYER_PARSER = new OlympaPlayerParser<>();

	private OlympaPlayerParser() {}

	public T parse(Player p, String msg) {
		Player target = Bukkit.getPlayer(msg);
		if (target == null) {
			Prefix.BAD.sendMessage(p, "Ce joueur n'est pas connecté.");
			return null;
		}
		return AccountProvider.get(target.getUniqueId());
	}

	public static <T extends OlympaPlayer> OlympaPlayerParser<T> parser() {
		return (OlympaPlayerParser<T>) OLYMPA_PLAYER_PARSER;
	}

}
