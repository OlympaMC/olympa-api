package fr.olympa.api.bungee.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.olympa.api.common.permission.list.OlympaAPIPermissionsBungee;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import io.github.waterfallmc.waterfall.event.ProxyDefineCommandsEvent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class BungeeCommandListenerWaterFall implements Listener {

	@EventHandler(priority = EventPriority.LOWEST)
	public void onProxyDefineCommands(ProxyDefineCommandsEvent event) {
		if (!(event.getReceiver() instanceof ProxiedPlayer))
			return;
		ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
		OlympaPlayer olympaPlayer;
		try {
			olympaPlayer = new AccountProviderAPI(player.getUniqueId()).get();
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		Map<String, Command> commands = event.getCommands();
		List<String> commandsToMove = new ArrayList<>();
		commands.forEach((name, command) -> {
			if (command instanceof BungeeCommand) {
				BungeeCommand bungeeCommand = (BungeeCommand) command;
				if (!bungeeCommand.hasPermission(olympaPlayer))
					commandsToMove.add(name);
			} else if ((command.getPermission() == null || !player.hasPermission(command.getPermission()))
					&& !OlympaAPIPermissionsBungee.BYPASS_PERM_NOT_EXIST.hasPermission(olympaPlayer))
				commandsToMove.add(name);
		});
		commandsToMove.forEach(name -> commands.remove(name));
	}

}
