package fr.olympa.api.command;

import java.util.List;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.OlympaCore;
import fr.olympa.api.objects.OlympaConsole;

public abstract class BanReflectCommand extends ReflectCommand {

	private OlympaCommand exe = null;

	public BanReflectCommand(String name) {
		super(name);
	}

	public BanReflectCommand(String name, String description, String usageMessage, List<String> aliases) {
		super(name, description, usageMessage, aliases);
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

		UUID author;
		if (sender instanceof Player) {
			author = this.exe.player.getUniqueId();
		} else {
			author = OlympaConsole.getUniqueId();
		}

		if (!this.exe.isAsynchronous) {
			return this.exe.onCommand(sender, this, label, args);
		} else {
			OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> this.exe.onCommand(sender, this, label, args));
			return true;
		}
	}

	@Override
	public void setExecutor(final OlympaCommand exe) {
		this.exe = exe;
	}

	@Override
	public List<String> tabComplete(final CommandSender sender, final String alias, final String[] args) {
		return this.exe.onTabComplete(sender, this, alias, args);
	}

}