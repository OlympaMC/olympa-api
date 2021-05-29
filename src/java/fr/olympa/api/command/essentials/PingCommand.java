package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Reflection;
import fr.olympa.api.utils.spigot.TPSUtils;

public class PingCommand extends OlympaCommand {

	public PingCommand(Plugin plugin) {
		super(plugin, "ping", "Permet d'obtenir son ping.", OlympaAPIPermissionsSpigot.PING_COMMAND);
		addArgs(false, "joueur");
		minArg = 0;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target;
		if (args.length == 0) {
			if (player == null) {
				sendImpossibleWithConsole();
				return false;
			}
			target = player;
		} else {
			target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				sendUnknownPlayer(args[0]);
				return false;
			}
		}
		if (player != null && player.getUniqueId().equals(target.getUniqueId()))
			Prefix.DEFAULT_GOOD.sendMessage(player, "Ton ping est de &2%sms&a.", TPSUtils.getPingColor(Reflection.ping(target)));
		else
			Prefix.DEFAULT_GOOD.sendMessage(sender, "Le ping de &2%s&a est de &2%sms&a.", target.getName(), TPSUtils.getPingColor(Reflection.ping(target)));
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
