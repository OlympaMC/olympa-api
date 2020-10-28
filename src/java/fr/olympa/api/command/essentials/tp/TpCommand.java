package fr.olympa.api.command.essentials.tp;

import java.text.DecimalFormat;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;

public class TpCommand extends OlympaCommand {

	public TpCommand(Plugin plugin) {
		super(plugin, "teleport", "Permet de se téléporter à un joueur ou une position.", OlympaAPIPermissions.TP_COMMAND, "tp");
		addArgs(false, "JOUEUR", "NUMBER");
		addArgs(false, "JOUEUR", "NUMBER");
		addArgs(false, "NUMBER");
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String targetName = null;
		Player target = null;
		Player source;
		if (args.length == 1) {
			source = player;
			targetName = args[0];
			target = Bukkit.getPlayer(targetName);
			if (target == null) {
				sendUnknownPlayer(targetName);
				return false;
			}
		} else if (args.length == 2) {
			targetName = args[0];
			source = Bukkit.getPlayer(targetName);
			if (source == null) {
				sendUnknownPlayer(targetName);
				return false;
			}
			targetName = args[1];
			target = Bukkit.getPlayer(targetName);
			if (target == null) {
				sendUnknownPlayer(targetName);
				return false;
			}
		} else if (args.length == 3) {
			source = player;
			Double x = (Double) RegexMatcher.DOUBLE.parse(args[0]);
			Double y = (Double) RegexMatcher.DOUBLE.parse(args[1]);
			Double z = (Double) RegexMatcher.DOUBLE.parse(args[2]);
			if (x == null || y == null || z == null) {
				sendError("La position x = %s, y = %s, z = %s n'est pas valide.", args[0], args[1], args[2]);
				return false;
			}
			Location location = new Location(player.getWorld(), x, y, z);
			player.teleport(location);
			String turne = AccountProvider.get(source.getUniqueId()).getGender().getTurne();
			DecimalFormat formatter = new DecimalFormat("0.#");
			Prefix.DEFAULT_GOOD.sendMessage(source, "&aTu as été téléporté%s en &2%s %s %s&a.", turne, formatter.format(location.getX()), formatter.format(location.getY()), formatter.format(location.getZ()));
			return true;
		} else {
			sendUsage(label);
			return false;
		}
		source.teleport(target);
		String turne = AccountProvider.get(source.getUniqueId()).getGender().getTurne();
		if (source != player)
			sendMessage(Prefix.DEFAULT_GOOD, "&2%s&a a été téléporté à &2%s&a.", turne, source.getName(), target.getName());
		Prefix.DEFAULT_GOOD.sendMessage(source, "&aTu as été téléporté%s à &2%s&a.", turne, target.getName());
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
