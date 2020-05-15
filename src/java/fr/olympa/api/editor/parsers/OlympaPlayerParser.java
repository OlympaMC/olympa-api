package fr.olympa.api.editor.parsers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
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
