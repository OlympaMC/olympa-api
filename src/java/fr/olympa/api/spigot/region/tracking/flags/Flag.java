package fr.olympa.api.spigot.region.tracking.flags;

import fr.olympa.api.spigot.region.tracking.ActionResult;
import fr.olympa.api.spigot.region.tracking.RegionEvent.EntryEvent;
import fr.olympa.api.spigot.region.tracking.RegionEvent.ExitEvent;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

public class Flag {

	private BaseComponent[] greeting, farewell;
	private ChatMessageType position;

	private ActionResult entry = ActionResult.ALLOW;
	private ActionResult exit = ActionResult.ALLOW;

	public Flag setMessages(String greeting, String farewell, ChatMessageType position) {
		this.position = position;
		this.greeting = greeting != null ? TextComponent.fromLegacyText(greeting) : null;
		this.farewell = farewell != null ? TextComponent.fromLegacyText(farewell) : null;
		return this;
	}

	public Flag setEntryExitDenied(boolean entryDenied, boolean exitDenied) {
		this.entry = entryDenied ? ActionResult.DENY : ActionResult.ALLOW;
		this.exit = exitDenied ? ActionResult.DENY : ActionResult.ALLOW;
		return this;
	}

	/**
	 * Appelé quand un joueur entre dans la région
	 * @param event
	 * @return <code>true </code> si le joueur ne peut pas entrer
	 */
	public ActionResult enters(EntryEvent event) {
		if (greeting != null) event.getPlayer().spigot().sendMessage(position, greeting);
		return entry;
	}

	/**
	 * Appelé quand un joueur sort d'une région
	 * @param event
	 * @return <code>true </code> si le joueur ne peut pas sortir
	 */
	public ActionResult leaves(ExitEvent event) {
		if (farewell != null) event.getPlayer().spigot().sendMessage(position, farewell);
		return exit;
	}

}
