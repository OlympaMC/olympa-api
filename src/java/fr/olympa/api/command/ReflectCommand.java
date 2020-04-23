package fr.olympa.api.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;

public class ReflectCommand extends Command {

	private OlympaCommand exe = null;

	public ReflectCommand(String name) {
		super(name);
	}

	public ReflectCommand(String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
	}

	@Override
	public boolean execute(CommandSender sender, String label, String[] args) {
		exe.sender = sender;
		if (sender instanceof Player) {
			exe.player = (Player) sender;
			if (exe.permission != null) {
				if (!exe.hasPermission()) {
					if (exe.getOlympaPlayer() == null) {
						exe.sendImpossibleWithOlympaPlayer();
					} else {
						exe.sendDoNotHavePermission();
					}
					return false;
				}
			}
		} else if (!exe.allowConsole) {
			exe.sendImpossibleWithConsole();
			return false;
		}
		if (args.length < exe.minArg) {
			exe.sendUsage(label);
			return false;
		}
		if (!exe.isAsynchronous) {
			return exe.onCommand(sender, this, label, args);
		} else {
			OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> exe.onCommand(sender, this, label, args));
			return true;
		}
	}

	public void setExecutor(OlympaCommand exe) {
		this.exe = exe;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (sender instanceof Player) {
			if (!exe.hasPermission()) {
				if (exe.getOlympaPlayer() == null) {
					exe.sendImpossibleWithOlympaPlayer();
				} else {
					exe.sendDoNotHavePermission();
				}
				return null;
			}
		} else if (!exe.allowConsole) {
			exe.sendImpossibleWithConsole();
			return null;
		}
		List<String> customResponse = exe.onTabComplete(sender, this, alias, args);
		if (customResponse != null) {
			return customResponse;
		}

		Collection<List<String>> defaultArgs = exe.args.values();
		if (!defaultArgs.isEmpty()) {
			Iterator<List<String>> iterator = defaultArgs.iterator();
			List<String> getArgs = null;
			List<String> potentialArgs = new ArrayList<>();
			int i = 0;
			while (iterator.hasNext()) {
				getArgs = iterator.next();
				i++;
			}
			if (args.length == i && getArgs != null) {
				for (String argss : getArgs) {
					switch (argss.toLowerCase()) {
					case "joueur":
						potentialArgs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
						break;
					case "time":
						potentialArgs.addAll(Arrays.asList("1h", "2h", "4h", "6h", "12h", "1jour", "2jours", "3jours", "1semaine", "2semaines", "1mois", "1an"));
						break;
					default:
						potentialArgs.add(argss);
						break;
					}
				}

				return Utils.startWords(args[i - 1], potentialArgs);
			}
		}
		return null;
	}

}
