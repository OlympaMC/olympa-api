package fr.olympa.api.command.essentials.tp;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import fr.olympa.api.command.OlympaCommand;

public class TpyesCommand extends OlympaCommand {

	private TpaHandler handler;
	
	TpyesCommand(TpaHandler handler) {
		super(handler.plugin, "tpayes", "Accepte la dernière demande de téléportation.", handler.permission, "tpyes", "tpaccept");
		this.handler = handler;
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		handler.acceptRequest(player);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		// TODO Auto-generated method stub
		return null;
	}

}
