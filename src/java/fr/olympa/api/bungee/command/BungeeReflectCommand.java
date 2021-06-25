package fr.olympa.api.bungee.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.common.command.CommandArgument;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.TabExecutor;

public class BungeeReflectCommand extends Command implements TabExecutor {

	BungeeCommand bungeecomand;

	public BungeeReflectCommand(String name, BungeeCommand bungeecomand, String permission, String[] aliases) {
		super(name, permission, aliases);
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxyServer.getInstance().getScheduler().runAsync(bungeecomand.plugin, () -> {
			bungeecomand.sender = sender;
			if (sender instanceof ProxiedPlayer) {
				bungeecomand.proxiedPlayer = (ProxiedPlayer) sender;
				try {
					bungeecomand.olympaPlayer = new AccountProviderAPI(bungeecomand.proxiedPlayer.getUniqueId()).get();
				} catch (SQLException e) {
					e.printStackTrace();
					bungeecomand.sendError("Impossible de récupérer tes données.");
					return;
				}
				if (!bungeecomand.bypassAuth && DataHandler.isUnlogged(bungeecomand.proxiedPlayer)) {
					bungeecomand.sendError("Tu dois être connecté. Fais &4/login <mdp>&c.");
					return;
				}

				if (bungeecomand.permission != null) {
					if (bungeecomand.olympaPlayer == null) {
						bungeecomand.sendImpossibleWithOlympaPlayer();
						return;
					}
					if (!bungeecomand.permission.hasPermission(bungeecomand.olympaPlayer)) {
						bungeecomand.sendDoNotHavePermission();
						return;
					}
				}
			} else {
				bungeecomand.proxiedPlayer = null;
				bungeecomand.olympaPlayer = null;
				if (!bungeecomand.allowConsole) {
					bungeecomand.sendImpossibleWithConsole();
					return;
				}
			}

			if (args.length < bungeecomand.minArg) {
				bungeecomand.sendUsage(getName());
				return;
			}
			bungeecomand.onCommand(sender, args);
		});

	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		bungeecomand.sender = sender;
		if (sender instanceof ProxiedPlayer) {
			bungeecomand.proxiedPlayer = (ProxiedPlayer) sender;
			if (!bungeecomand.hasPermission())
				return new ArrayList<>();
		} else {
			bungeecomand.proxiedPlayer = null;
			if (!bungeecomand.allowConsole) {
				bungeecomand.sendImpossibleWithConsole();
				return new ArrayList<>();
			}
		}
		Iterable<String> customResponse = bungeecomand.onTabComplete(sender, bungeecomand, args);
		if (customResponse != null)
			return customResponse;
		Set<List<CommandArgument>> defaultArgs = bungeecomand.args.keySet();
		if (defaultArgs.isEmpty())
			return new ArrayList<>();
		Iterator<List<CommandArgument>> iterator = defaultArgs.iterator();
		List<CommandArgument> cas = null;
		List<String> potentialArgs = new ArrayList<>();
		int i = 0;
		while (iterator.hasNext() && args.length > i) {
			cas = iterator.next();
			i++;
		}
		if (args.length != i || cas == null)
			return new ArrayList<>();
		for (CommandArgument ca : cas) {
			if (ca.getPermission() != null && !ca.getPermission().hasPermission(bungeecomand.getOlympaPlayer()) || !ca.hasRequireArg(args, i))
				continue;
			switch (ca.getArgName().toUpperCase()) {
			case "CONFIGS":
				potentialArgs.addAll(BungeeCustomConfig.getConfigs().stream().map(BungeeCustomConfig::getName).collect(Collectors.toList()));
				break;
			case "JOUEUR":
				potentialArgs.addAll(ProxyServer.getInstance().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()));
				break;
			case "TIME":
				potentialArgs.addAll(Arrays.asList("1h", "2h", "4h", "6h", "12h", "1j", "2j", "3j", "1semaine", "2semaines", "1mois", "1an"));
				break;
			//			case "SERVERS":
			//				potentialArgs.addAll(MonitorServers.getServers().stream().map(MonitorInfoBungee::getName).collect(Collectors.toList()));
			default:
				potentialArgs.add(ca.getArgName());
				break;
			}
		}
		return Utils.startWords(args[i - 1], potentialArgs);
	}

}
