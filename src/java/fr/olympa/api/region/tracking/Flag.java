package fr.olympa.api.region.tracking;

import org.bukkit.entity.Player;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Flag {

	private final BaseComponent[] greeting;
	private final BaseComponent[] farewell;
	private ChatMessageType position;

	public Flag() {
		this(null, null, null);
	}

	public Flag(String greeting, String farewell, ChatMessageType position) {
		this.position = position;
		this.greeting = TextComponent.fromLegacyText(greeting);
		this.farewell = TextComponent.fromLegacyText(farewell);
	}

	/**
	 * Appelé quand un joueur entre dans la région
	 * @param p Joueur qui entre dans la région
	 * @return <code>true </code> si le joueur ne peut pas entrer
	 */
	public boolean enters(Player p) {
		if (greeting != null) p.spigot().sendMessage(position, greeting);
		return false;
	}

	/**
	 * Appelé quand un joueur sort d'une région
	 * @param p Joueur qui sort de la région
	 * @return <code>true </code> si le joueur ne peut pas sortir
	 */
	public boolean leaves(Player p) {
		if (farewell != null) p.spigot().sendMessage(position, farewell);
		return false;
	}

}
