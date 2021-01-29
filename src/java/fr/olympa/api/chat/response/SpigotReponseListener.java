package fr.olympa.api.chat.response;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.server.ServerCommandEvent;

import fr.olympa.api.chat.sender.ISender;

public class SpigotReponseListener implements Listener {

	@EventHandler
	public void onChat(AsyncPlayerChatEvent event) {
		ReponseEvent.messageReceive(ISender.of(event.getPlayer(), null), event.getMessage());
	}

	@EventHandler
	public void onServerChat(ServerCommandEvent event) {
		ReponseEvent.messageReceive(ISender.of(event.getSender(), null), event.getCommand());
	}
}
