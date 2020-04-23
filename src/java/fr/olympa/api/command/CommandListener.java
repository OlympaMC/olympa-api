package fr.olympa.api.command;

import java.util.Arrays;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class CommandListener implements Listener {

	@EventHandler
	public boolean onPlayerCommand(PlayerCommandPreprocessEvent event) {
		String[] message = event.getMessage().substring(0).split(" ");

		String command = message[0].toLowerCase();
		OlympaCommand cmd = OlympaCommand.commandPreProcess.get(command);
		if (cmd == null) {
			return false;
		}
		Player player = event.getPlayer();
		cmd.onCommand(player, null, command, Arrays.copyOfRange(message, 1, message.length));
		return false;
	}
}
