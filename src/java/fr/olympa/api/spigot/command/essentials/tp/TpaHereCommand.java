package fr.olympa.api.spigot.command.essentials.tp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.spigot.command.OlympaCommand;

public class TpaHereCommand extends OlympaCommand {

	private TpaHandler handler;

	TpaHereCommand(TpaHandler handler) {
		super(handler.plugin, "tpahere", "Permet de téléporter un autre joueur à soi.", handler.permission, "tphere");
		this.handler = handler;
		addArgs(true, "joueur");
		minArg = 1;
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sendUnknownPlayer(args[0]);
			return false;
		}
		handler.sendRequestHere(player, target);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
