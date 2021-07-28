package fr.olympa.api.spigot.feedback;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.ComplexCommand;

public class FeedbackCommand extends ComplexCommand {
	
	public FeedbackCommand(Plugin plugin) {
		super(plugin, "feedback", "Envoie un rapport de bug ou de suggestion au staff.", OlympaAPIPermissionsSpigot.FEEDBACK_COMMAND, "retour", "bug");
	}
	
	@Override
	public boolean noArguments(CommandSender sender) {
		return true;
	}
	
}
