package fr.olympa.api.bungee.command;

import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ChatEvent;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeCommandListener implements Listener {

	@EventHandler(priority = -128)
	public void onChat(ChatEvent event) {
		String message = event.getMessage();
		if (!message.startsWith("/"))
			return;
		String[] args = message.substring(1).split(" ");
		if (args.length == 0)
			return;
		String command = args[0].toLowerCase();
		BungeeCommand cmd = BungeeCommand.commandPreProcess.entrySet().stream().filter(entry -> entry.getKey().contains(command)).map(Entry::getValue).findFirst().orElse(null);
		if (cmd == null)
			return;
		event.setCancelled(true);
		ProxiedPlayer player = (ProxiedPlayer) event.getSender();
		cmd.execute(player, Arrays.copyOfRange(args, 1, args.length));
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onTabComplete(TabCompleteEvent event) {
		List<String> sugg = event.getSuggestions();
		if (sugg.isEmpty())
			return;
		String command = sugg.get(0).toLowerCase();
		BungeeCommand cmd = BungeeCommand.commandPreProcess.entrySet().stream().filter(entry -> entry.getKey().contains(command)).map(Entry::getValue).findFirst().orElse(null);
		if (cmd == null)
			return;
		if (cmd.hasPermission(AccountProvider.<OlympaPlayer>get(((ProxiedPlayer) event.getSender()).getUniqueId()))) {
			//CommandSender sender = (CommandSender) event.getSender();
			//		sugg.remove(0);
			List<String> suggestion = (List<String>) cmd.onTabComplete((CommandSender) event.getSender(), sugg.toArray(String[]::new));
			if (!suggestion.isEmpty()) {
				event.getSuggestions().clear();
				event.getSuggestions().addAll(suggestion);
			}
		} else
			event.setCancelled(true);
	}
}
