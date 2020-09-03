package fr.olympa.api.command.essentials.tp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;

public class TpHereConfirmCommand extends OlympaCommand {

	private TpaHandler handler;

	TpHereConfirmCommand(TpaHandler handler) {
		super(handler.plugin, "tphereyes", "Refuse la dernière demande de téléportation.", handler.permission, "tphereyes", "tparefuse");
		this.handler = handler;
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player target = Bukkit.getPlayer(args[0]);
		if (target == null) {
			sendUnknownPlayer(args[0]);
			return false;
		}
		handler.refuseRequest(player, target);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
