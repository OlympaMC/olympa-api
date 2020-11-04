package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.utils.Prefix;

public class InvseeCommand extends OlympaCommand {

	public InvseeCommand(Plugin plugin) {
		super(plugin, "invsee", OlympaAPIPermissions.INVSEE_COMMAND);
		addArgs(true, "JOUEUR");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sendUnknownPlayer(args[0]);
			return false;
		}
		player.openInventory(target.getInventory());
		Prefix.DEFAULT_GOOD.sendMessage(target, "&aOuverture de l'inventaire de &2%s&a.", target.getName());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
