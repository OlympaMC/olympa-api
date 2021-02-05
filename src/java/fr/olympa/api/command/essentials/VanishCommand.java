package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.vanish.IVanishApi;
import fr.olympa.core.spigot.OlympaCore;

public class VanishCommand extends OlympaCommand {

	public VanishCommand(Plugin plugin) {
		super(plugin, "vanish", "Permet de se mettre en Vash", OlympaAPIPermissions.VANISH_COMMAND, new String[] {});
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		IVanishApi vanishApi = OlympaCore.getInstance().getVanishApi();
		if (vanishApi.isVanished(player))
			vanishApi.disable(getOlympaPlayer(), true);
		else
			vanishApi.enable(getOlympaPlayer(), true);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}
