package fr.olympa.api.spigot.command.essentials;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.OlympaCommand;

public class FlyCommand extends OlympaCommand {

	public FlyCommand(Plugin plugin) {
		super(plugin, "fly", "Permet d'activer/désactiver la possibilité de voler.", OlympaAPIPermissionsSpigot.FLY_COMMAND);
		addArgs(false, "JOUEUR");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target;
		if (args.length == 0) {
			if (isConsole()) {
				sendImpossibleWithConsole();
				return false;
			}
			target = player;
		}else {
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				sendUnknownPlayer(args[0]);
				return false;
			}
		}
		target.setAllowFlight(!target.getAllowFlight());
		sendSuccess("Tu es désormais en %s&a.", target.getAllowFlight() ? "§2fly on" : "§cfly off");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
