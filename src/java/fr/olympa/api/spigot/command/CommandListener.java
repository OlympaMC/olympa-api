package fr.olympa.api.spigot.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class CommandListener implements Listener {

	private static List<String> removeTabCommand = Arrays.asList("pardon-ip", "ban", "pardon", "spigot", "icanhasbukkit");
	private static List<String> removePermissionToCommand = Arrays.asList("aac", "icanhasbukkit", "version", "spigot", "icanhasbukkit", "protocolsupport", "jumppads", "viaversion");

	@EventHandler
	public void onCommandSend(PlayerCommandSendEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = AccountProviderAPI.getter().get(player.getUniqueId());
		OlympaCommand.commands.stream().filter(cmd -> !cmd.hasPermission(olympaPlayer)).forEach(cmd -> event.getCommands().removeAll(cmd.getAllCommands()));
		Arrays.stream(Bukkit.getServer().getPluginManager().getPlugins()).map(Plugin::getDescription).forEach(plDesc -> {
			plDesc.getCommands().forEach((command, data) -> {
				String permission = (String) data.get("permission");
				Object aliasesObject = data.get("aliases");
				List<String> commandAndAliases = new ArrayList<>();
				commandAndAliases.add(command);
				if (aliasesObject != null)
					if (aliasesObject instanceof String)
						commandAndAliases.add((String) aliasesObject);
					else if (aliasesObject instanceof List)
						commandAndAliases.addAll((Collection<? extends String>) aliasesObject);
				if ((permission == null || !player.hasPermission(permission))
						&& !OlympaAPIPermissionsSpigot.BYPASS_PERM_NOT_EXIST.hasPermission(olympaPlayer))
					event.getCommands().removeAll(commandAndAliases);

			});
		});
		event.getCommands().removeAll(removeTabCommand);
	}

	@EventHandler(priority = EventPriority.LOW)
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
		if (command.contains(":") && !OlympaAPIPermissionsSpigot.NAMESPACED_COMMANDS.hasSenderPermission(sender)) {
			Prefix.DEFAULT_BAD.sendMessage(sender, "Par mesure de sécurité, les commandes avec namespace sont désactivées.");
			event.setCancelled(true);
			return;
		}
		if (command.equalsIgnoreCase("op") && !(sender instanceof ConsoleCommandSender)) {
			event.setCancelled(true);
			Prefix.DEFAULT_BAD.sendMessage(sender, "Désolé, on va éviter de laisser traîner cette commande...");
			return;
		}
		OlympaCommand cmd = OlympaCommand.commandPreProcess.entrySet().stream().filter(entry -> entry.getKey().contains(command)).map(entry -> entry.getValue()).findFirst().orElse(null);
		if (cmd != null) {
			event.setCancelled(true);
			sendcommand(cmd, message, sender);
		}
		@Nullable
		Command nativeCommand = OlympaCommand.getCommandMap().getCommand(command);
		if (nativeCommand == null)
			return;
		if (nativeCommand.getPermission() != null && !sender.hasPermission(nativeCommand.getPermission()) && !OlympaAPIPermissionsSpigot.BYPASS_PERM_NOT_EXIST.hasSenderPermission(sender)) {

		}
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
