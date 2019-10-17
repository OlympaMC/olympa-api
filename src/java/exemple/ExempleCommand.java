package exemple;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.OlympaCorePermissions;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ExempleCommand extends OlympaCommand {

	// NOTE: you didn't need to add the command in plugin.yml
	public ExempleCommand(Plugin plugin) {
		super(plugin, "exemple", OlympaCorePermissions.CHAT_COMMAND, "alias1", "alias2");

		// Use to create the usage message. This will be prefixed by "Usage: /%command%"
		this.setUsageString("<arg1> <arg2> [arg3]");

		// if sendr enter less than x args, the sender will receive the usage message
		this.setMinArg(2);

		// Allows Console and Command Block to use the command
		this.setAllowConsole(true);

	}

	@SuppressWarnings("unused")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// null if sender = Console or Command Block
		Player player = this.player;

		// == AccountProvider.get(player)
		// null if sender = Console or Command Block
		OlympaPlayer olympaPlayer = this.getOlympaPlayer();

		// send messages
		this.sendMessage("To you only");
		this.sendMessageToAll("[ALL] Hello everyone & Console"); // include Console
		this.sendMessage(sender, "");
		this.sendMessage(new ComponentBuilder("Hello ").color(ChatColor.RED).bold(true).append("world").color(ChatColor.DARK_RED).append("!").color(ChatColor.RED).create());
		this.broadcast("[ALL] Hello players"); // Broadcast to players only

		// send pre-made messages
		this.sendMessage(Prefix.DEFAULT, "This is the default prefix.");
		this.sendDoNotHavePermission();
		this.sendError("You can't blabla");
		// useful to avoid making an error if the message is sent to Gui, or
		// BaseComponent (JSON) messages
		this.sendImpossibleWithConsole();
		this.sendImpossibleWithOlympaPlayer();
		this.sendUnknownPlayer("playerName");
		// send the Usage message. label = command name
		this.sendUsage(label);

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
