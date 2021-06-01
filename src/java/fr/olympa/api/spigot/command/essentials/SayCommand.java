package fr.olympa.api.spigot.command.essentials;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.OlympaCommand;

public class SayCommand extends OlympaCommand {
	
	public SayCommand(Plugin plugin) {
		super(plugin, "say", "Envoie un message dans le chat.", OlympaAPIPermissionsSpigot.SAY_COMMAND);
		minArg = 1;
		usageString = "<message>";
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		String msg = String.join(" ", args);
		String text = "§d§k##§6 " + (isConsole() ? "§6CONSOLE" : getOlympaPlayer().getGroup().getColor() + player.getName()) + " §7➤ " + msg;
		int receivers = Bukkit.broadcastMessage(text);
		sendSuccess("Message envoyé. (%d receveurs) ", receivers);
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
