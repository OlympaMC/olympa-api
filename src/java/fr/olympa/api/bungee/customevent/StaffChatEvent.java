package fr.olympa.api.bungee.customevent;

import fr.olympa.api.common.player.OlympaPlayer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Event;

public class StaffChatEvent extends Event {

	final private OlympaPlayer olympaPlayer;
	final private CommandSender sender;
	final private String message;

	public StaffChatEvent(CommandSender sender, OlympaPlayer olympaPlayer, String message) {
		this.sender = sender;
		this.olympaPlayer = olympaPlayer;
		this.message = message;
	}

	@SuppressWarnings("unchecked")
	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return (T) olympaPlayer;
	}

	public CommandSender getSender() {
		return sender;
	}

	public String getMessage() {
		return message;
	}
}
