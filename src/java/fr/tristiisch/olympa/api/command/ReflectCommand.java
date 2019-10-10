package fr.tristiisch.olympa.api.command;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import exemple.Main;

public class ReflectCommand extends Command {

	private OlympaCommand exe = null;

	protected ReflectCommand(final String command) {
		super(command);
	}

	@Override
	public boolean execute(final CommandSender sender, final String label, final String[] args) {
		this.exe.sender = sender;
		if (sender instanceof Player) {
			this.exe.player = (Player) sender;

			if (this.exe.permission != null) {
				if (this.exe.getOlympaPlayer() == null) {
					this.exe.sendImpossibleWithOlympaPlayer();
					return false;
				}

				if (!this.exe.hasPermission()) {
					this.exe.sendDoNotHavePermission();
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
			Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), () -> this.exe.onCommand(sender, this, label, args));
			return true;
		}
	}

	public void setExecutor(final OlympaCommand exe) {
		this.exe = exe;
	}

	@Override
	public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
		return this.exe.onTabComplete(sender, this, alias, args);
	}
}