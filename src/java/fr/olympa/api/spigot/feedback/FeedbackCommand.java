package fr.olympa.api.spigot.feedback;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.spigot.command.ComplexCommand;

public class FeedbackCommand extends ComplexCommand {
	
	private Map<Player, FeedbackBuilder> builders = new HashMap<>();
	
	public FeedbackCommand(Plugin plugin) {
		super(plugin, "feedback", "Envoie un rapport de bug ou de suggestion au staff.", OlympaAPIPermissionsSpigot.FEEDBACK_COMMAND, "retour", "bug");
		setAllowConsole(false);
	}
	
	@Override
	public boolean noArguments(CommandSender sender) {
		FeedbackBuilder builder = new FeedbackBuilder(getPlayer());
		builders.put(getPlayer(), builder);
		return true;
	}
	
	@Cmd (hide = true, min = 1)
	public void next(CommandContext cmd) {
		FeedbackBuilder builder = builders.get(getPlayer());
		if (builder == null) {
			sendError("Ce créateur de retour n'est plus disponible...");
			return;
		}
		if (!builder.next(cmd.getFrom(1))) builders.remove(getPlayer());
	}
	
	@Cmd (hide = true)
	public void prev(CommandContext cmd) {
		FeedbackBuilder builder = builders.get(getPlayer());
		if (builder == null) {
			sendError("Ce créateur de retour n'est plus disponible...");
			return;
		}
		builder.previous();
	}
	
	@Cmd (hide = true)
	public void exit(CommandContext cmd) {
		builders.remove(getPlayer());
	}
	
	@Cmd (permissionName = "FEEDBACK_COMMAND_SEE")
	public void view(CommandContext cmd) {
		new FeedbackChooseGUI().create(getPlayer());
	}
	
}
