package fr.olympa.api.bungee.command;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.bungee.player.DataHandler;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.command.CommandArgument;
import fr.olympa.api.common.command.IOlympaCommand;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.TabExecutor;

public abstract class BungeeCommand extends Command implements IOlympaCommand, TabExecutor {

	static Map<List<String>, BungeeCommand> commandPreProcess = new HashMap<>();

	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		return new ArrayList<>();
	}

	public abstract void onCommand(CommandSender sender, String[] args);

	protected String[] aliases;
	public boolean allowConsole = true;
	protected String command;
	protected String description;
	protected boolean bypassAuth = false;
	protected OlympaBungeePermission permission;
	protected Map<List<CommandArgument>, Boolean> args = new LinkedHashMap<>();
	List<String> commands = new ArrayList<>();

	/**
	 * Don't foget to set {@link NewBungeeCommand#usageString}
	 */
	public Integer minArg = 0;

	protected Plugin plugin;
	protected CommandSender sender;
	protected ProxiedPlayer proxiedPlayer;
	protected OlympaPlayer olympaPlayer;

	/**
	 * Format: Usage » %command% <%obligatory%|%obligatory%> [%optional%] Variable
	 * name: 'joueur' ...
	 *
	 */
	public String usageString;

	public BungeeCommand(Plugin plugin, String command) {
		super(command);
		this.plugin = plugin;
		this.command = command;
	}

	public BungeeCommand(Plugin plugin, String command, String description, OlympaBungeePermission permission, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.description = description;
		this.command = command;
		this.permission = permission;
		this.aliases = aliases;
	}

	public BungeeCommand(Plugin plugin, String command, OlympaBungeePermission permission, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.aliases = aliases;
	}

	public BungeeCommand(Plugin plugin, String command, OlympaBungeePermission permission, String[] aliases, String description, String usageString, boolean allowConsole, Integer minArg) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.aliases = aliases;
		this.description = description;
		this.usageString = usageString;
		this.allowConsole = allowConsole;
		this.minArg = minArg;
	}

	public BungeeCommand(Plugin plugin, String command, String... aliases) {
		super(command, null, aliases);
		this.plugin = plugin;
		this.command = command;
		this.aliases = aliases;
	}

	@Override
	public String buildText(int min, String[] args) {
		return String.join(" ", Arrays.copyOfRange(args, min, args.length));
	}

	@Override
	public void execute(CommandSender sender, String[] args) {
		ProxyServer.getInstance().getScheduler().runAsync(plugin, () -> {
			this.sender = sender;
			if (sender instanceof ProxiedPlayer) {
				proxiedPlayer = (ProxiedPlayer) sender;
				try {
					olympaPlayer = new AccountProviderAPI(proxiedPlayer.getUniqueId()).get();
				} catch (SQLException e) {
					e.printStackTrace();
					sendError("Impossible de récupérer tes données.");
					return;
				}
				if (!bypassAuth && DataHandler.isUnlogged(proxiedPlayer)) {
					sendError("Tu dois être connecté. Fais &4/login <mdp>&c.");
					return;
				}

				if (permission != null) {
					if (olympaPlayer == null) {
						sendImpossibleWithOlympaPlayer();
						return;
					}
					if (!permission.hasPermission(olympaPlayer)) {
						sendDoNotHavePermission();
						return;
					}
				}
			} else {
				proxiedPlayer = null;
				olympaPlayer = null;
				if (!allowConsole) {
					sendImpossibleWithConsole();
					return;
				}
			}

			if (args.length < minArg) {
				sendUsage(getName());
				return;
			}
			onCommand(sender, args);
		});

	}

	@Override
	public Iterable<String> onTabComplete(CommandSender sender, String[] args) {
		this.sender = sender;
		if (sender instanceof ProxiedPlayer) {
			proxiedPlayer = (ProxiedPlayer) sender;
			if (!this.hasPermission())
				return new ArrayList<>();
		} else {
			proxiedPlayer = null;
			if (!allowConsole) {
				sendImpossibleWithConsole();
				return new ArrayList<>();
			}
		}
		List<String> customResponse = onTabComplete(this.sender, this, args);
		if (customResponse != null)
			return customResponse;
		Set<List<CommandArgument>> defaultArgs = this.args.keySet();
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
			if (ca.getPermission() != null && !ca.getPermission().hasPermission(getOlympaPlayer()) || !ca.hasRequireArg(args, i))
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

	public String getCommand() {
		return command;
	}

	@Override
	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	public ProxiedPlayer getProxiedPlayer() {
		return proxiedPlayer;
	}

	@Override
	public BungeeCommand register() {
		build();
		plugin.getProxy().getPluginManager().registerCommand(plugin, this);
		return this;
	}

	@Override
	public BungeeCommand registerPreProcess() {
		build();
		commands.add(command);
		commands.addAll(Arrays.asList(aliases));
		commandPreProcess.put(commands, this);
		return this;
	}

	@Override
	public void unregister() {
		plugin.getProxy().getPluginManager().unregisterCommand(this);
		if (commandPreProcess.containsValue(this))
			commandPreProcess.remove(commands, this);
	}

	@Override
	public void sendDoNotHavePermission() {
		sendError("Tu as pas la permission &l(◑_◑)");
	}

	public void sendError(String message) {
		this.sendMessage(Prefix.DEFAULT_BAD, message);
	}

	@Override
	public void sendImpossibleWithConsole() {
		sendError("Impossible avec la console.");
	}

	@Override
	public void sendImpossibleWithOlympaPlayer() {
		sendError("Une erreur est survenu avec tes donnés.");
	}

	public static void sendMessage(CommandSender sender, Prefix prefix, String text) {
		sendMessage(sender, prefix + ColorUtils.color(text));
	}

	public static void sendMessage(CommandSender sender, String text) {
		sender.sendMessage(TextComponent.fromLegacyText(ColorUtils.color(text)));
	}

	public void sendMessage(Prefix prefix, String text) {
		sendMessage(sender, prefix, text);
	}

	public void sendMessage(String text) {
		sendMessage(sender, ColorUtils.color(text));
	}

	public void sendMessage(TextComponent text) {
		sender.sendMessage(text);
	}

	@Override
	public void sendUsage(String label) {
		sendMessage(Prefix.USAGE, "/%s %s", label, usageString);
	}

	@Deprecated
	public void sendUsage() {
		this.sendMessage(Prefix.USAGE, "/" + command + " " + usageString);
	}

	@Override
	public void sendMessage(Prefix prefix, String message, Object... args) {
		sender.sendMessage(TextComponent.fromLegacyText(prefix.formatMessage(message, args)));

	}

	public void sendMessage(CommandSender sender, Prefix prefix, String text, Object... args) {
		sendMessage(new HashSet<>(Arrays.asList(sender)), prefix, text, args);
	}

	public void sendMessage(Iterable<? extends CommandSender> senders, Prefix prefix, String text, Object... args) {
		text = prefix.formatMessage(text, args);
		for (CommandSender s : senders)
			sendMessage(s, text);
	}

	@Override
	public int broadcast(Prefix prefix, String text, Object... args) {
		Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
		sendMessage(players, prefix, text, args);
		return players.size();
	}

	@Override
	public int broadcastToAll(Prefix prefix, String text, Object... args) {
		sendMessage(ProxyServer.getInstance().getConsole(), prefix, text, args);
		return broadcast(prefix, text, args) + 1;
	}

	@Override
	public void sendComponents(BaseComponent... components) {
		sender.sendMessage(components);
	}

	@Override
	public ProxiedPlayer getPlayer() {
		return proxiedPlayer;
	}

	@Override
	public CommandSender getSender() {
		return sender;
	}

	@Override
	public boolean isConsole() {
		return proxiedPlayer == null;
	}

	@Override
	public OlympaPermission getOlympaPermission() {
		return permission;
	}

	@Override
	public void setAllowConsole(boolean allowConsole) {
		this.allowConsole = allowConsole;
	}

	@Override
	public boolean isConsoleAllowed() {
		return allowConsole;
	}

	@Override
	public void addCommandArguments(boolean isMandatory, List<CommandArgument> ca) {
		args.put(ca, isMandatory);
	}

	public void sendHelp(CommandSender sender) {
		sendMessage(sender, Prefix.DEFAULT, "§eCommande §6%s", command + (aliases == null || aliases.length == 0 ? "" : " §e(" + ColorUtils.joinGold(aliases) + ")"));
		if (description != null)
			sendMessage(sender, Prefix.DEFAULT, "§e%s", description);
	}

	private void build() {
		if (!args.isEmpty())
			usageString = args.entrySet().stream().map(entry -> {
				boolean isMandatory = entry.getValue();
				List<CommandArgument> ca = entry.getKey();
				return (isMandatory ? "<" : "[") + ca.stream().map(c -> c.getArgName()).collect(Collectors.joining("|")) + (isMandatory ? ">" : "]");
			}).collect(Collectors.joining(" "));
		if (minArg == 0)
			minArg = (int) args.entrySet().stream().count();
	}
}
