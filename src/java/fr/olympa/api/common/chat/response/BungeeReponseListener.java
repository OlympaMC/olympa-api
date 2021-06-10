package fr.olympa.api.common.chat.response;

import fr.olympa.api.common.chat.sender.ISender;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class BungeeReponseListener implements Listener {

	@EventHandler
	public void onChat(ChatEvent event) {
		if (event.getSender() instanceof CommandSender)
			ReponseEvent.messageReceive(ISender.of(event.getSender(), null), event.getMessage());
	}
}
