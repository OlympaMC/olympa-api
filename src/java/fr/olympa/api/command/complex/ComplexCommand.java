package fr.olympa.api.command.complex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ComplexCommand extends OlympaCommand {

	protected static final HoverEvent COMMAND_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§bSuggérer la commande."));

	private static final List<String> INTEGERS = Arrays.asList("1", "2", "3", "...");
	private static final List<String> BOOLEAN = Arrays.asList("true", "false");

	public class InternalCommand {
		public Cmd cmd;
		public OlympaPermission perm;
		public Method method;
		public Object commands;
		public String name;

		InternalCommand(Cmd cmd, Method method, Object commandsClass) {
			this.cmd = cmd;
			this.method = method;
			commands = commandsClass;
			perm = OlympaPermission.permissions.get(cmd.permissionName());
			name = method.getName();
		}

		boolean canRun() {
			return hasPermission(perm) && (!cmd.player() || !isConsole());
		}
	}

	private class ArgumentParser {
		private Function<CommandSender, List<String>> tabArgumentsFunction;
		private Function<String, Object> supplyArgumentFunction;

		public ArgumentParser(Function<CommandSender, List<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction) {
			this.tabArgumentsFunction = tabArgumentsFunction;
			this.supplyArgumentFunction = supplyArgumentFunction;
		}
	}

	public final Map<List<String>, InternalCommand> commands = new HashMap<>();

	public InternalCommand getCommand(String argName) {
		return commands.entrySet().stream().filter(entry -> entry.getKey().contains(argName.toLowerCase())).findFirst().map(entry -> entry.getValue()).orElse(null);
	}

	public boolean containsCommand(String argName) {
		return commands.entrySet().stream().anyMatch(entry -> entry.getKey().contains(argName.toLowerCase()));
	}

	private final Map<String, ArgumentParser> parsers = new HashMap<>();

	public ComplexCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... alias) {
		super(plugin, command, description, permission, alias);

		addArgumentParser("PLAYERS", sender -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), x -> {
			Player result = Bukkit.getPlayerExact(x);
			if (result == null)
				sendUnknownPlayer(x);
			return result;
		});
		addArgumentParser("INTEGER", sender -> INTEGERS, x -> {
			try {
				return Integer.parseInt(x);
			} catch (NumberFormatException e) {
				sendError(x + " doit être un nombre entier.");
			}
			return null;
		});
		addArgumentParser("DOUBLE", sender -> Collections.EMPTY_LIST, x -> {
			try {
				return Double.parseDouble(x);
			} catch (NumberFormatException e) {
				sendError(x + " doit être un nombre décimal.");
			}
			return null;
		});
		addArgumentParser("BOOLEAN", sender -> BOOLEAN, Boolean::parseBoolean);
		addArgumentParser("WORLD", sender -> Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()), x -> {
			World result = Bukkit.getWorld(x);
			if (result == null)
				sendError("Le monde %s n'existe pas.", x);
			return result;
		});
		addArgumentParser("SUBCOMMAND", sender -> commands.keySet().stream().flatMap(List::stream).collect(Collectors.toList()), x -> {
			InternalCommand result = getCommand(x);
			if (result == null)
				sendError("La commande %s n'existe pas.", x);
			return result;
		});

		registerCommandsClass(this);
	}

	public <T extends Enum<T>> void addArgumentParser(String name, Class<T> enumClass) {
		List<String> values = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
		addArgumentParser(name, sender -> values, playerInput -> {
			for (T each : enumClass.getEnumConstants())
				if (each.name().equalsIgnoreCase(playerInput))
					return each;
			sendError("La valeur %s n'existe pas.", playerInput);
			return null;
		});
	}

	public void addArgumentParser(String name, Function<CommandSender, List<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction) {
		parsers.put(name, new ArgumentParser(tabArgumentsFunction, supplyArgumentFunction));
	}

	public boolean noArguments(CommandSender sender) {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			if (!noArguments(sender))
				this.sendIncorrectSyntax();
			return true;
		}

		InternalCommand internal = getCommand(args[0]);
		if (internal == null) {
			sendError("La commande n'existe pas.");
			return true;
		}

		Cmd cmd = internal.cmd;
		if (cmd.player() && !(sender instanceof Player)) {
			sendImpossibleWithConsole();
			return true;
		}

		if (!hasPermission(internal.perm)) {
			sendDoNotHavePermission();
			return true;
		}

		if (args.length - 1 < cmd.min()) {
			if ("".equals(cmd.syntax()))
				this.sendIncorrectSyntax();
			else
				this.sendIncorrectSyntax(internal.method.getName() + " " + cmd.syntax());
			return true;
		}

		Object[] argsCmd = new Object[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			String arg = args[i];
			String[] types = (i > cmd.args().length ? "" : cmd.args()[i - 1]).split("\\|");
			Object result = null;
			ArgumentParser parser = parsers.entrySet().stream().filter(entry -> Arrays.stream(types)
					.anyMatch(type -> entry.getKey().equals(type) && entry.getValue().tabArgumentsFunction.apply(sender).contains(arg)))
					.map(Entry::getValue).findFirst().orElse(null);
			if (parser != null)
				result = parser.supplyArgumentFunction.apply(arg);
			else
				result = arg;
			if (result == null)
				return true;
			argsCmd[i - 1] = result;
		}

		try {
			internal.method.invoke(internal.commands, new CommandContext(this, argsCmd, label));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sendError("Une erreur est survenue.");
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> tmp = new ArrayList<>();
		List<String> find = new ArrayList<>();
		String sel = args[0];

		if (args.length == 1)
			for (Entry<List<String>, InternalCommand> en : commands.entrySet()) { // PERMISSIONS
				if (!en.getValue().cmd.hide() || en.getValue().canRun())
					find.add(en.getKey().get(0));
			}
		else if (args.length >= 2) {
			int index = args.length - 2;
			if (!containsCommand(sel))
				return tmp;
			InternalCommand internal = getCommand(sel);
			String[] needed = internal.cmd.args();
			if (needed.length <= index)
				return tmp;
			if (!internal.cmd.permissionName().isEmpty() && !internal.perm.hasSenderPermission(sender))
				return tmp;
			sel = args[index + 1];
			String[] types = needed[index].split("\\|");
			for (String type : types) {
				ArgumentParser parser = parsers.get(type);
				if (parser != null)
					find.addAll(parser.tabArgumentsFunction.apply(sender));
				else
					find.addAll(Arrays.asList(type));
			}
		} else
			return tmp;

		for (String arg : find)
			if (arg.toLowerCase().startsWith(sel.toLowerCase()))
				tmp.add(arg);
		return tmp;
	}

	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	public void registerCommandsClass(Object commandsClassInstance) {
		Class<?> clazz = commandsClassInstance.getClass();
		do
			registerCommandsClass(clazz, commandsClassInstance);
		while ((clazz = clazz.getSuperclass()) != null);
	}

	private void registerCommandsClass(Class<?> clazz, Object commandsClassInstance) {
		for (Method method : clazz.getDeclaredMethods())
			if (method.isAnnotationPresent(Cmd.class)) {
				Cmd cmd = method.getDeclaredAnnotation(Cmd.class);
				if (method.getParameterCount() == 1)
					if (method.getParameterTypes()[0] == CommandContext.class) {
						List<String> argNames = new ArrayList<>();
						argNames.add(method.getName().toLowerCase());
						if (cmd.aliases() != null)
							argNames.addAll(Arrays.asList(cmd.aliases()));
						commands.put(argNames, new InternalCommand(cmd, method, commandsClassInstance));
						continue;
					}
				OlympaCore.getInstance()
						.sendMessage("Error when loading command annotated method " + method.getName() + " in class " + method.getDeclaringClass().getName() + ". Required argument: fr.olympa.api.command.complex.CommandContext");
			}
	}

	@Override
	public void sendHelp(CommandSender sender) {
		super.sendHelp(sender);
		for (InternalCommand command : commands.values()) {
			if (command.cmd.hide() || !command.canRun())
				continue;
			sender.spigot().sendMessage(getHelpCommandComponent(command));
		}
	}

	@Cmd(args = "SUBCOMMAND", syntax = "[commande]")
	public void help(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			sendHelp(sender);
		else {
			InternalCommand command = cmd.getArgument(0);
			if (!command.canRun()) {
				sendIncorrectSyntax();
				return;
			}
			sender.spigot().sendMessage(getHelpCommandComponent(command));
		}
	}

	private TextComponent getHelpCommandComponent(InternalCommand command) {
		String fullCommand = "/" + super.command + " " + command.name;

		TextComponent component = new TextComponent();
		component.setHoverEvent(COMMAND_HOVER);
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, fullCommand + " "));

		for (BaseComponent commandCompo : TextComponent.fromLegacyText("§7➤ §6" + fullCommand + " §e" + command.cmd.syntax()))
			component.addExtra(commandCompo);

		return component;
	}

}