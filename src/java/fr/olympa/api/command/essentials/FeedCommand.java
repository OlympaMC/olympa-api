package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaSpigotPermission;

public class FeedCommand extends OlympaCommand {

	public FeedCommand(Plugin plugin, OlympaSpigotPermission permission) {
		super(plugin, "feed", "Restaure entièrement la barre de faim de l'utilisateur.", permission);
		setAllowConsole(false);
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		getPlayer().setFoodLevel(20);
		getPlayer().setSaturation(20);
		sendSuccess("Ta barre de faim a été restautée et sa saturation est au maximum.");
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}

}
