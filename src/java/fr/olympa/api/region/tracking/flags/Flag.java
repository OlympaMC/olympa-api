package fr.olympa.api.region.tracking.flags;

import java.util.Set;

import org.bukkit.entity.Player;

import fr.olympa.api.region.tracking.TrackedRegion;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Flag {

	private BaseComponent[] greeting;
	private BaseComponent[] farewell;
	private ChatMessageType position;

	private boolean entryDenied = false;
	private boolean exitDenied = false;

	public Flag setMessages(String greeting, String farewell, ChatMessageType position) {
		this.position = position;
		this.greeting = greeting != null ? TextComponent.fromLegacyText(greeting) : null;
		this.farewell = farewell != null ? TextComponent.fromLegacyText(farewell) : null;
		return this;
	}

	public Flag setEntryExitDenied(boolean entryDenied, boolean exitDenied) {
		this.entryDenied = entryDenied;
		this.exitDenied = exitDenied;
		return this;
	}

	/**
	 * Appelé quand un joueur entre dans la région
	 * @param p Joueur qui entre dans la région
	 * @param to Liste des régions applicables au joueur après son entrée
	 * @return <code>true </code> si le joueur ne peut pas entrer
	 */
	public boolean enters(Player p, Set<TrackedRegion> to) {
		if (greeting != null) p.spigot().sendMessage(position, greeting);
		return entryDenied;
	}

	/**
	 * Appelé quand un joueur sort d'une région
	 * @param p Joueur qui sort de la région
	 * @param to Liste des régions applicables au joueur après sa sortie
	 * @return <code>true </code> si le joueur ne peut pas sortir
	 */
	public boolean leaves(Player p, Set<TrackedRegion> to) {
		if (farewell != null) p.spigot().sendMessage(position, farewell);
		return exitDenied;
	}

}
