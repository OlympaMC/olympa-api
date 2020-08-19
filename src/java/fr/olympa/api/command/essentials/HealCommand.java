package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaPermission;

public class HealCommand extends OlympaCommand {

	public HealCommand(Plugin plugin, OlympaPermission permission) {
		super(plugin, "heal", "Restaure entièrement la vie de l'utilisateur.", permission);
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		getPlayer().setHealth(getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
		sendSuccess("Ta vie a été restaurée.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
