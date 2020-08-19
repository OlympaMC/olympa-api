package fr.olympa.api.command.essentials.tp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;

public class TpaCommand extends OlympaCommand {

	private TpaHandler handler;
	
	TpaCommand(TpaHandler handler) {
		super(handler.plugin, "tpa", "Permet de se téléporter à un autre joueur.", handler.permission, "tpto");
		this.handler = handler;
		addArgs(true, "joueur");
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sendUnknownPlayer(args[0]);
			return false;
		}
		handler.sendRequestTo(player, target);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
