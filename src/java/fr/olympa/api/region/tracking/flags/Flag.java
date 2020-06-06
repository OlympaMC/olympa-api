package fr.olympa.api.region.tracking.flags;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Flag {

	private BaseComponent[] greeting;
	private BaseComponent[] farewell;
	private ChatMessageType position;

	public Flag setMessages(String greeting, String farewell, ChatMessageType position) {
		this.position = position;
		this.greeting = greeting != null ? TextComponent.fromLegacyText(greeting) : null;
		this.farewell = farewell != null ? TextComponent.fromLegacyText(farewell) : null;
		return this;
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

	public void onEvent(Event event) {}

}
