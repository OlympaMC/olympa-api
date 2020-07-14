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

public class ComplexCommand extends OlympaCommand {

	private static final List<String> INTEGERS = Arrays.asList("1", "2", "3", "...");
	private static final List<String> BOOLEAN = Arrays.asList("true", "false");

	public class InternalCommand {
		public Cmd cmd;
		public OlympaPermission perm;
		public Method method;
		public Object commands;

		InternalCommand(Cmd cmd, Method method, Object commandsClass) {
			this.cmd = cmd;
			perm = OlympaPermission.permissions.get(cmd.permissionName());
			this.method = method;
			commands = commandsClass;
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
			if (result == null) sendError("Le monde %s n'existe pas.", x);
			return result;
		});
		
		registerCommandsClass(this);
	}

	public <T extends Enum<T>> void addArgumentParser(String name, Class<T> enumClass) {
		List<String> values = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
		addArgumentParser(name, sender -> values, playerInput -> {
			for (T each : enumClass.getEnumConstants()) {
				if (each.name().equalsIgnoreCase(playerInput)) return each;
			}
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

		if (!cmd.permissionName().isEmpty() && !internal.perm.hasSenderPermission(sender)) {
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
			String type = i > cmd.args().length ? "" : cmd.args()[i - 1];
			Object result = null;
			ArgumentParser parser = parsers.get(type);
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
				String perm = en.getValue().cmd.permissionName();
				if (perm != null && this.hasPermission(perm))
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
			String type = needed[index];
			ArgumentParser parser = parsers.get(type);
			if (parser != null)
				find.addAll(parser.tabArgumentsFunction.apply(sender));
			else
				find.addAll(Arrays.asList(type.split("\\|")));
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
		for (Method method : commandsClassInstance.getClass().getMethods())
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

}