package fr.olympa.api.command;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Reflection;
import net.md_5.bungee.api.chat.BaseComponent;

@SuppressWarnings("unchecked")
public abstract class OlympaCommand implements IOlympaCommand {

	protected static Map<List<String>, OlympaCommand> commandPreProcess = new HashMap<>();
	protected static CommandMap cmap;

	protected Plugin plugin;
	protected List<String> aliases;
	protected LinkedHashMap<Boolean, List<CommandArgument>> args = new LinkedHashMap<>();
	protected String command;
	protected String description;
	protected OlympaPermission permission;
	protected Player player;
	protected CommandSender sender;
	protected String usageString;
	protected Integer minArg;
	protected boolean allowConsole = true;
	protected boolean isAsynchronous = false;

	public OlympaCommand(Plugin plugin, String command, OlympaPermission permission, String... aliases) {
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

	public OlympaCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... aliases) {
		this.plugin = plugin;
		this.permission = permission;
		this.command = command;
		this.description = description;
		this.aliases = Arrays.asList(aliases);
	}

	@Override
	public void addCommandArguments(boolean isMandatory, List<CommandArgument> ca) {
		args.put(isMandatory, ca);
	}

	@Override
	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return AccountProvider.get(player.getUniqueId());
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
	public void setAllowConsole(boolean allowConsole) {
		this.allowConsole = allowConsole;
	}

	@Override
	public boolean hasPermission() {
		return this.hasPermission(permission);
	}

	public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

	public abstract List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args);

	private void build() {
		usageString = args.entrySet().stream().map(entry -> {
			boolean isMandatory = entry.getKey();
			List<CommandArgument> ca = entry.getValue();
			return (isMandatory ? "<" : "[") + ca.stream().map(c -> c.getArgName()).collect(Collectors.joining("|")) + (isMandatory ? ">" : "]");
		}).collect(Collectors.joining(" "));
		if (minArg == null)
			minArg = (int) args.entrySet().stream().filter(entry -> entry.getKey()).count();
	}

	@Override
	public void register() {
		build();
		ReflectCommand reflectCommand = new ReflectCommand(command);
		if (aliases != null)
			reflectCommand.setAliases(aliases);
		if (description != null)
			reflectCommand.setDescription(description);
		reflectCommand.setExecutor(this);
		getCommandMap().register("Olympa", reflectCommand);
	}

	@Override
	public void registerPreProcess() {
		build();
		List<String> commands = new ArrayList<>();
		commands.add(command);
		commands.addAll(aliases);
		commandPreProcess.put(commands, this);
	}

	public void sendMessage(Iterable<? extends CommandSender> senders, Prefix prefix, String text, Object... args) {
		text = prefix.formatMessage(text, args);
		for (CommandSender sender : senders)
			sender.sendMessage(text);
	}

	@Override
	public void broadcast(Prefix prefix, String text, Object... args) {
		sendMessage(Bukkit.getOnlinePlayers(), prefix, text, args);
	}

	@Override
	public void broadcastToAll(Prefix prefix, String text, Object... args) {
		broadcast(prefix, text, args);
		prefix.sendMessage(Bukkit.getConsoleSender(), text, args);
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

	@Override
	public boolean isConsole() {
		return player == null;
	}

	@Override
	public boolean isConsoleAllowed() {
		return allowConsole;
	}
}
