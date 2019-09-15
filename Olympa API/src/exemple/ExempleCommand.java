package exemple;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.tristiisch.olympa.api.command.OlympaCommand;
import fr.tristiisch.olympa.api.permission.OlympaPermission;

public class ExempleCommand extends OlympaCommand {

	public ExempleCommand(Plugin plugin) {
		super(plugin, "exemple", OlympaPermission.CHAT_COMMAND, "alias1", "alias2");
		this.setUsageString("<arg1> <arg2> [arg3]");
		this.setMinArg(2);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
