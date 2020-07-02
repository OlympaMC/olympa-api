package fr.olympa.api.region.tracking;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.core.spigot.OlympaCore;

public class BypassCommand extends OlympaCommand {
	
	public static List<Player> bypasses = new ArrayList<>();
	
	public BypassCommand() {
		super(OlympaCore.getInstance(), "bypassregions", "Permet de ne pas être affecté par les restrictions de régions.", OlympaAPIPermissions.COMMAND_BYPASS_REGIONS, "bypass");
		super.setAllowConsole(false);
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (bypasses.remove(player)) {
			sendSuccess("Tu ne bypass plus les régions.");
		}else {
			bypasses.add(player);
			sendSuccess("Tu bypass désormais les régions.");
		}
		return false;
	}
	
	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
	
}
