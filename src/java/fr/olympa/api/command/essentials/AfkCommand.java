package fr.olympa.api.command.essentials;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.afk.AfkPlayer;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.core.spigot.OlympaCore;

public class AfkCommand extends OlympaCommand {

	public AfkCommand(Plugin plugin) {
		super(plugin, "afk", "Permet de se mettre AFK", null, new String[] {});
		allowConsole = false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		AfkHandler afkHandler = OlympaCore.getInstance().getAfkHandler();
		AfkPlayer afkPlayer = afkHandler.get(player);
		if (afkPlayer.isAfk())
			afkPlayer.setNotAfk(player);
		else
			afkPlayer.setAfk(player);
		return false;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
		return null;
	}
}