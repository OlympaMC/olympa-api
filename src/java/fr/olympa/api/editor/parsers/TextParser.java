package fr.olympa.api.editor.parsers;

import org.bukkit.entity.Player;

@FunctionalInterface
public abstract interface TextParser<T> {

	public abstract T parse(Player p, String msg);
	
}
