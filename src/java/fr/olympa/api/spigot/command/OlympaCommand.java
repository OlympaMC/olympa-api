package fr.olympa.api.spigot.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.command.CommandArgument;
import fr.olympa.api.common.command.IOlympaCommand;
import fr.olympa.api.common.permission.OlympaPermission;
import fr.olympa.api.common.permission.OlympaSpigotPermission;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.utils.Reflection;
import fr.olympa.api.utils.Prefix;
import net.md_5.bungee.api.chat.BaseComponent;

@SuppressWarnings("unchecked")
public abstract class OlympaCommand implements IOlympaCommand {

	public static List<OlympaCommand> commands = new ArrayList<>();
	protected static Map<List<String>, OlympaCommand> commandPreProcess = new HashMap<>();
	protected static CommandMap cmap;

	protected Plugin plugin;
	protected List<String> aliases;
	protected LinkedHashMap<List<CommandArgument>, Boolean> args = new LinkedHashMap<>();
	protected List<String> allCommands;
	protected String command;
	protected String description;
	protected OlympaSpigotPermission permission;
	protected Player player;
	protected CommandSender sender;
	protected String usageString;
	protected Integer minArg;
	protected boolean allowConsole = true;
	protected boolean isAsynchronous = false;
	public ReflectCommand reflectCommand;

	public OlympaCommand(Plugin plugin, String command, OlympaSpigotPermission permission, String... aliases) {
		this.plugin = plugin;
		this.command = command;
		this.permission = permission;
		this.aliases = Arrays.asList(aliases);
	}

	public OlympaCommand(Plugin plugin, String command, String... aliases) {
		this.plugin = plugin;
		this.command = command;
		this.aliases = Arrays.asList(aliases);
	}

	public OlympaCommand(Plugin plugin, String command, String description, OlympaSpigotPermission permission, String... aliases) {
		this.plugin = plugin;
		this.permission = permission;
		this.command = command;
		this.description = description;
		this.aliases = Arrays.asList(aliases);
	}

	public String getCommand() {
		return command;
	}

	public List<String> getAliases() {
		return aliases;
	}

	public List<String> getAllCommands() {
		return allCommands;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public void addCommandArguments(boolean isMandatory, List<CommandArgument> ca) {
		args.put(ca, isMandatory);
	}

	@Override
	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return AccountProviderAPI.getter().get(player.getUniqueId());
	}

	@Override
	public Player getPlayer() {
		return player;
	}

	@Override
	public CommandSender getSender() {
		return sender;
	}

	@Override
	public boolean isConsole() {
		return player == null;
	}

	@Override
	public boolean isConsoleAllowed() {
		return allowConsole;
	}

	@Override
	public void setAllowConsole(boolean allowConsole) {
		this.allowConsole = allowConsole;
	}

	@Override
	public OlympaPermission getOlympaPermission() {
		return permission;
	}

	@Override
	public boolean hasPermission() {
		if (isConsole())
			return true;
		return IOlympaCommand.super.hasPermission();
	}

	@Override
	public boolean hasPermission(OlympaPermission perm) {
		if (isConsole())
			return true;
		return IOlympaCommand.super.hasPermission(perm);
	}

	public boolean hasPermission(CommandSender sender) {
		if (sender instanceof Player)
			return hasPermission(AccountProviderAPI.getter().<OlympaPlayer>get(((Player) sender).getUniqueId()));
		else
			return isConsoleAllowed();
	}

	public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

	private void build() {
		usageString = args.entrySet().stream().map(entry -> {
			boolean isMandatory = entry.getValue();
			List<CommandArgument> ca = entry.getKey();
			return (isMandatory ? "<" : "[") + ca.stream().map(c -> c.getArgName()).collect(Collectors.joining("|")) + (isMandatory ? ">" : "]");
		}).collect(Collectors.joining(" "));
		if (minArg == null)
			minArg = (int) args.entrySet().stream().filter(entry -> entry.getValue()).count();
		allCommands = new ArrayList<>();
		allCommands.add(command);
		allCommands.addAll(aliases);
	}

	@Override
	public OlympaCommand register() {
		build();
		ReflectCommand reflectCommand = new ReflectCommand(command);
		if (aliases != null)
			reflectCommand.setAliases(aliases);
		if (description != null)
			reflectCommand.setDescription(description);
		reflectCommand.setExecutor(this);
		this.reflectCommand = reflectCommand;
		getCommandMap().register("Olympa", reflectCommand);
		if (!commands.contains(this)) {
			commands.add(this);
			Collections.sort(commands, (o1, o2) -> o1.command.compareTo(o2.command));
		}
		return this;
	}

	@Override
	public OlympaCommand registerPreProcess() {
		build();
		commandPreProcess.put(allCommands, this);
		if (!commands.contains(this)) {
			commands.add(this);
			Collections.sort(commands, (o1, o2) -> o1.command.compareTo(o2.command));
		}
		return this;
	}

	@Override
	public void unregister() {
		if (commands.contains(this))
			commands.remove(this);
		if (commandPreProcess.containsValue(this))
			commandPreProcess.remove(allCommands, this);
	}

	public void sendMessage(Iterable<? extends CommandSender> senders, Prefix prefix, String text, Object... args) {
		text = prefix.formatMessage(text, args);
		for (CommandSender sender : senders)
			sender.sendMessage(text);
	}

	@Override
	public int broadcast(Prefix prefix, String text, Object... args) {
		Collection<? extends Player> players = Bukkit.getOnlinePlayers();
		sendMessage(players, prefix, text, args);
		return players.size();
	}

	@Override
	public int broadcastToAll(Prefix prefix, String text, Object... args) {
		prefix.sendMessage(Bukkit.getConsoleSender(), text, args);
		return broadcast(prefix, text, args) + 1;
	}

	@Override
	public void sendComponents(BaseComponent... components) {
		sender.spigot().sendMessage(components);
	}

	@Override
	public void sendMessage(Prefix prefix, String message, Object... args) {
		prefix.sendMessage(sender, message, args);
	}

	@Override
	public void sendUsage(String label) {
		sendMessage(Prefix.USAGE, "/%s %s", label, usageString);
	}

	public void sendHelp(CommandSender sender) {
		Prefix.DEFAULT.sendMessage(sender, "&eCommande &6%s", command + (aliases == null || aliases.isEmpty() ? "" : " &e(" + ColorUtils.joinGoldEt(aliases) + ")"));
		if (description != null)
			Prefix.DEFAULT.sendMessage(sender, "&e%s", description);
	}

	public static void unRegisterCommands(OlympaAPIPlugin plugin) {
		commands.removeIf(oc -> oc.plugin.equals(plugin));
		Set<Entry<List<String>, OlympaCommand>> cmdPluginProcess = commandPreProcess.entrySet().stream().filter(oc -> oc.getValue().plugin.equals(plugin)).collect(Collectors.toSet());
		cmdPluginProcess.forEach(oc -> commandPreProcess.remove(oc.getKey()));

	}

	public static void unRegisterCommand(PluginCommand... cmds) {
		try {
			SimpleCommandMap commandMap = (SimpleCommandMap) getCommandMap();
			Object map = Reflection.getField(commandMap.getClass(), "knownCommands");
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
			for (PluginCommand cmd : cmds) {
				knownCommands.remove(cmd.getName());
				for (String alias : cmd.getAliases())
					knownCommands.remove(alias);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void unRegisterCommand(PluginCommand cmd) {
		try {
			SimpleCommandMap commandMap = (SimpleCommandMap) getCommandMap();
			Object map = Reflection.getField(commandMap.getClass(), "knownCommands");
			HashMap<String, Command> knownCommands = (HashMap<String, Command>) map;
			knownCommands.remove(cmd.getName());
			for (String alias : cmd.getAliases())
				knownCommands.remove(alias);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static CommandMap getCommandMap() {
		if (cmap == null)
			try {
				Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
				f.setAccessible(true);
				cmap = (CommandMap) f.get(Bukkit.getServer());
				f.setAccessible(false);
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		return cmap;
	}

}
