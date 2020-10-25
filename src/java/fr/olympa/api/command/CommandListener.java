package fr.olympa.api.command;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;

import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class CommandListener implements Listener {

	@EventHandler
	public void onCommandSend(PlayerCommandSendEvent e) {
		OlympaPlayer player = AccountProvider.get(e.getPlayer().getUniqueId());
		OlympaCommand.commands.stream().filter(cmd -> cmd.hasPermission(player)).forEach(cmd -> e.getCommands().removeAll(cmd.getAllCommands()));
	}
	
	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		processCommand(event, event.getMessage().substring(1), event.getPlayer());
	}

	@EventHandler
	public void onServerCommand(ServerCommandEvent event) {
		processCommand(event, event.getCommand(), event.getSender());
	}

	private void processCommand(Cancellable event, String fullCommand, CommandSender sender) {
		if (event.isCancelled())
			return;
		String[] message = fullCommand.split(" ");

		String command = message[0].toLowerCase();
		if (command.contains(":") && !OlympaAPIPermissions.NAMESPACED_COMMANDS.hasSenderPermission(sender)) {
			Prefix.DEFAULT_BAD.sendMessage(sender, "Par mesure de sécurité, les commandes avec namespace sont désactivées.");
			event.setCancelled(true);
			return;
		}
		OlympaCommand cmd = OlympaCommand.commandPreProcess.entrySet().stream().filter(entry -> entry.getKey().contains(command)).map(entry -> entry.getValue()).findFirst().orElse(null);
		if (cmd == null)
			return;
		event.setCancelled(true);
		sendcommand(cmd, message, sender);
	}

	private void sendcommand(OlympaCommand exe, String[] args, CommandSender sender) {
		String label = args[0];
		exe.sender = sender;
		if (sender instanceof Player) {
			exe.player = (Player) sender;
			if (exe.permission != null)
				if (!exe.hasPermission()) {
					if (exe.getOlympaPlayer() == null)
						exe.sendImpossibleWithOlympaPlayer();
					else
						exe.sendDoNotHavePermission();
					return;
				}
		} else {
			exe.player = null;
			if (!exe.allowConsole) {
				exe.sendImpossibleWithConsole();
				return;
			}
		}
		if (args.length - 1 < exe.minArg) {
			exe.sendUsage(label);
			return;
		}
		if (!exe.isAsynchronous)
			exe.onCommand(sender, null, label, Arrays.copyOfRange(args, 1, args.length));
		else
			OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> exe.onCommand(sender, null, label, Arrays.copyOfRange(args, 1, args.length)));
	}
}
