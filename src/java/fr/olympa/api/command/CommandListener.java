package fr.olympa.api.command;

import java.util.Arrays;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import fr.olympa.core.spigot.OlympaCore;

public class CommandListener implements Listener {

	@EventHandler
	public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
		if (event.isCancelled())
			return;
		String[] message = event.getMessage().substring(1).split(" ");

		String command = message[0].toLowerCase();
		OlympaCommand cmd = OlympaCommand.commandPreProcess.entrySet().stream().filter(entry -> entry.getKey().contains(command)).map(entry -> entry.getValue()).findFirst().orElse(null);
		if (cmd == null)
			return;
		event.setCancelled(true);
		sendcommand(cmd, message, event.getPlayer());
	}

	@EventHandler
	public void onServerCommand(ServerCommandEvent event) {
		if (event.isCancelled())
			return;
		String[] message = event.getCommand().split(" ");

		String command = message[0].toLowerCase();
		OlympaCommand cmd = OlympaCommand.commandPreProcess.entrySet().stream().filter(entry -> entry.getKey().contains(command)).map(entry -> entry.getValue()).findFirst().orElse(null);
		if (cmd == null)
			return;
		event.setCancelled(true);
		sendcommand(cmd, message, event.getSender());
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
		}else {
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
