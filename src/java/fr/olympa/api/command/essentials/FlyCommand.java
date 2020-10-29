package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.utils.Prefix;

public class FlyCommand extends OlympaCommand {

	public FlyCommand(Plugin plugin) {
		super(plugin, "fly", "Permet de voler", OlympaAPIPermissions.FLY_COMMAND);
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		player.setAllowFlight(!player.getAllowFlight());
		sendMessage(Prefix.DEFAULT_GOOD, "Tu es désormais en %s&a.", player.getAllowFlight() ? "§2fly on" : "§cfly off");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
