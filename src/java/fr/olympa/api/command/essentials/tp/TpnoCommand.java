package fr.olympa.api.command.essentials.tp;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;

public class TpnoCommand extends OlympaCommand {

	private TpaHandler handler;
	
	TpnoCommand(TpaHandler handler) {
		super(handler.plugin, "tpano", "Refuse la dernière demande de téléportation.", handler.permission, "tpno", "tparefuse");
		this.handler = handler;
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		handler.refuseRequest(player);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
