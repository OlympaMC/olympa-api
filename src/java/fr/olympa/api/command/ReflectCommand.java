package fr.olympa.api.command;

import java.util.ArrayList;
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
		this.exe.sender = sender;
		if (sender instanceof Player) {
			this.exe.player = (Player) sender;
			if (this.exe.permission != null) {
				if (!this.exe.hasPermission()) {
					if (this.exe.getOlympaPlayer() == null) {
						this.exe.sendImpossibleWithOlympaPlayer();
					} else {
						this.exe.sendDoNotHavePermission();
					}
					return false;
				}
			}
		} else if (!this.exe.allowConsole) {
			this.exe.sendImpossibleWithConsole();
			return false;
		}
		if (args.length < this.exe.minArg) {
			this.exe.sendUsage(label);
			return false;
		}
		if (!this.exe.isAsynchronous) {
			return this.exe.onCommand(sender, this, label, args);
		} else {
			OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> this.exe.onCommand(sender, this, label, args));
			return true;
		}
	}

	public void setExecutor(OlympaCommand exe) {
		this.exe = exe;
	}

	@Override
	public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
		if (sender instanceof Player) {
			if (!this.exe.hasPermission()) {
				if (this.exe.getOlympaPlayer() == null) {
					this.exe.sendImpossibleWithOlympaPlayer();
				} else {
					this.exe.sendDoNotHavePermission();
				}
				return null;
			}
		} else if (!this.exe.allowConsole) {
			this.exe.sendImpossibleWithConsole();
			return null;
		}
		Collection<List<String>> defaultArgs = this.exe.args.values();
		if (!defaultArgs.isEmpty()) {
			Iterator<List<String>> iterator = defaultArgs.iterator();
			List<String> getArgs;
			List<String> potentialArgs = new ArrayList<>();
			int i = 1;
			do {
				getArgs = iterator.next();
			} while (++i < args.length && defaultArgs.size() <= i);
			if (args.length == i) {
				for (String argss : getArgs) {
					if (argss.equalsIgnoreCase("joueur")) {
						potentialArgs.addAll(Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()));
					} else {
						potentialArgs.add(argss);
					}
				}

				return Utils.startWords(args[i - 1], getArgs);
			}
		}
		return this.exe.onTabComplete(sender, this, alias, args);
	}

}
