package fr.olympa.api.command.essentials.tp;

import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.utils.Prefix;

public class TpConfirmCommand extends OlympaCommand {

	private TpaHandler handler;

	TpConfirmCommand(TpaHandler handler) {
		super(handler.plugin, "tpayes", "Accepte ou refuse la demande de téléportation d'un joueur.", handler.permission, "tpyes", "tpaccept", "tpano", "tpno", "tprefuse");
		this.handler = handler;
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		Player creator;
		if (args.length == 0) {
			creator = handler.getCreatorByTarget(player);
			if (creator == null) {
				sendMessage(Prefix.DEFAULT_BAD, "Tu n'a pas de demande de téléportation en attente.");
				return false;
			}
		} else {
			creator = Bukkit.getPlayer(args[0]);
			if (creator == null) {
				sendUnknownPlayer(args[0]);
				return false;
			}
		}
		if (label.contains("yes") || label.contains("accept"))
			handler.acceptRequest(player, creator);
		else if (label.contains("no") || label.contains("refuse"))
			handler.refuseRequest(player, creator);
		else
			sendError();
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
