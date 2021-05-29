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

public class EcseeCommand extends OlympaCommand {

	public EcseeCommand(Plugin plugin) {
		super(plugin, "ecvsee", "Pour voir l'enderchest d'un joueur.", OlympaAPIPermissionsSpigot.ECSEE_COMMAND);
		addArgs(true, "JOUEUR");
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sendUnknownPlayer(args[0]);
			return false;
		}
		player.openInventory(target.getEnderChest());
		Prefix.DEFAULT_GOOD.sendMessage(target, "&aOuverture de l'enderchest de &2%s&a.", target.getName());
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
