package exemple;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;

public class ExempleCommand extends OlympaCommand {

	// NOTE: you didn't need to add the command in plugin.yml
	public ExempleCommand(Plugin plugin) {
		super(plugin, "exemple", ExemplePermissions.EXEMPLE_COMMAND, "alias1", "alias2");

		// Use to create the usage message like "Usage: /%command% <arg1Mandatory|arg1MandatoryAgain|arg1MandatoryAgainAgain> [arg2|arg2Again|arg2AgainAgain]"
		// isMandatory = if args is required
		this.addArgs(true, "arg1Mandatory", "arg1MandatoryAgain", "arg1MandatoryAgainAgain");
		this.addArgs(false, "arg2", "arg2Again", "arg2AgainAgain");
		// if sendr enter less than x args, the sender will receive the usage message
		// this.setMinArg(1); not needed now it detect auto with this.addArgs(true, ...)

		// Allows Console and Command Block to use the command
		setAllowConsole(true);

	}

	@SuppressWarnings("unused")
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// null if sender = Console or Command Block
		Player player = this.player;

		// == AccountProvider.get(player)
		// null if sender = Console or Command Block
		OlympaPlayer olympaPlayer = this.getOlympaPlayer();

		// send pre-made messages
		this.sendMessage(Prefix.DEFAULT, "This is the default prefix.");
		sendDoNotHavePermission();
		// useful to avoid making an error if the message is sent to Gui, or
		// BaseComponent (JSON) messages
		sendImpossibleWithConsole();
		sendImpossibleWithOlympaPlayer();
		this.sendUnknownPlayer("playerName");

		// send messages
		broadcastToAll(Prefix.DEFAULT, "[ALL] Hello everyone & Console"); // include Console
		broadcast(Prefix.DEFAULT, "[ALL] Hello players"); // Broadcast to players only
		sendSuccess("Succès, argument %s", "coucou");
		// MSG JSON with spigot api
		this.sendComponents(Prefix.DEFAULT_GOOD, new ComponentBuilder("Hello ").color(ChatColor.RED).bold(true).append("world").color(ChatColor.DARK_RED).append("!").color(ChatColor.RED).create());
		// MSG JSON with olympa api
		sendComponents(new TxtComponentBuilder("&4It's the message. Move your mouse over it.").onHoverText("&eClic to excute the commande !").onClickCommand("/suicide").build());// everywhere
		sendHoverAndCommand(Prefix.DEFAULT_GOOD, "It's the message 2.0. Move your mouse over it.", "&eClic to excute the commande !", "/suicide"); // in class implements IOlympaCommand only
		this.sendError("The error %s is bad.", "404");

		// send the Usage message. label = command name
		sendUsage(label);

		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
