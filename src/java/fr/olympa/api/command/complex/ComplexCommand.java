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
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
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
			this.perm = OlympaPermission.permissions.get(cmd.permissionName());
			this.method = method;
			this.commands = commandsClass;
		}
	}

	private class ArgumentParser {
		private Supplier<List<String>> tabArgumentsSupplier;
		private Function<String, Object> supplyArgumentFunction;

		public ArgumentParser(Supplier<List<String>> tabArgumentsSupplier, Function<String, Object> supplyArgumentFunction) {
			this.tabArgumentsSupplier = tabArgumentsSupplier;
			this.supplyArgumentFunction = supplyArgumentFunction;
		}
	}

	public final Map<String, InternalCommand> commands = new HashMap<>();

	private final Map<String, ArgumentParser> parsers = new HashMap<>();

	public ComplexCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... alias) {
		super(plugin, command, description, permission, alias);

		addArgumentParser("PLAYERS", () -> Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), x -> {
			Player result = Bukkit.getPlayerExact(x);
			if (result == null) sendUnknownPlayer(x);
			return result;
		});
		addArgumentParser("INTEGER", () -> INTEGERS, x -> {
			try {
				return Integer.parseInt(x);
			}catch (NumberFormatException e) {
				sendError(x + " doit être un nombre entier.");
			}
			return null;
		});
		addArgumentParser("DOUBLE", () -> Collections.EMPTY_LIST, x -> {
			try {
				return Double.parseDouble(x);
			}catch (NumberFormatException e) {
				sendError(x + " doit être un nombre décimal.");
			}
			return null;
		});
		addArgumentParser("BOOLEAN", () -> BOOLEAN, Boolean::parseBoolean);
		
		this.registerCommandsClass(this);
	}

	public void addArgumentParser(String name, Supplier<List<String>> tabArgumentsSupplier, Function<String, Object> supplyArgumentFunction) {
		parsers.put(name, new ArgumentParser(tabArgumentsSupplier, supplyArgumentFunction));
	}

	public boolean noArguments(CommandSender sender) {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			if (!noArguments(sender)) this.sendIncorrectSyntax();
			return true;
		}

		InternalCommand internal = this.commands.get(args[0].toLowerCase());
		if (internal == null) {
			this.sendError("La commande n'existe pas.");
			return true;
		}

		Cmd cmd = internal.cmd;
		if (cmd.player() && !(sender instanceof Player)) {
			this.sendImpossibleWithConsole();
			return true;
		}

		if (!cmd.permissionName().isEmpty() && !internal.perm.hasSenderPermission(sender)) {
			this.sendDoNotHavePermission();
			return true;
		}

		if (args.length - 1 < cmd.min()) {
			if ("".equals(cmd.syntax())) {
				this.sendIncorrectSyntax();
			} else {
				this.sendIncorrectSyntax(internal.method.getName() + " " + cmd.syntax());
			}
			return true;
		}

		Object[] argsCmd = new Object[args.length - 1];
		for (int i = 1; i < args.length; i++) {
			String arg = args[i];
			String type = i > cmd.args().length ? "" : cmd.args()[i - 1];
			Object result = null;
			ArgumentParser parser = parsers.get(type);
			if (parser != null) {
				result = parser.supplyArgumentFunction.apply(arg);
			} else {
				result = arg;
			}
			if (result == null) return true;
			argsCmd[i - 1] = result;
		}

		try {
			internal.method.invoke(internal.commands, new CommandContext(this, argsCmd, label));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			this.sendError("Une erreur est survenue.");
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> tmp = new ArrayList<>();
		List<String> find = new ArrayList<>();
		String sel = args[0];

		if (args.length == 1) {
			for (Entry<String, InternalCommand> en : this.commands.entrySet()) { // PERMISSIONS
				String perm = en.getValue().cmd.permissionName();
				if (perm != null && this.hasPermission(perm)) {
					find.add(en.getKey());
				}
			}
		} else if (args.length >= 2) {
			int index = args.length - 2;
			if (!this.commands.containsKey(sel)) {
				return tmp;
			}
			InternalCommand internal = this.commands.get(sel);
			String[] needed = internal.cmd.args();
			if (needed.length <= index) {
				return tmp;
			}
			if (!internal.cmd.permissionName().isEmpty() && !internal.perm.hasSenderPermission(sender)) {
				return tmp;
			}
			sel = args[index + 1];
			String type = needed[index];
			ArgumentParser parser = parsers.get(type);
			if (parser != null) {
				find.addAll(parser.tabArgumentsSupplier.get());
			} else {
				find.addAll(Arrays.asList(type.split("\\|")));
			}
		} else {
			return tmp;
		}

		for (String arg : find) {
			if (arg.startsWith(sel)) {
				tmp.add(arg);
			}
		}
		return tmp;
	}

	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	public void registerCommandsClass(Object commandsClassInstance) {
		for (Method method : commandsClassInstance.getClass().getDeclaredMethods()) {
			if (method.isAnnotationPresent(Cmd.class)) {
				Cmd cmd = method.getDeclaredAnnotation(Cmd.class);
				if (method.getParameterCount() == 1) {
					if (method.getParameterTypes()[0] == CommandContext.class) {
						this.commands.put(method.getName().toLowerCase(), new InternalCommand(cmd, method, commandsClassInstance));
						continue;
					}
				}
				OlympaCore.getInstance()
						.sendMessage("Error when loading command annotated method " + method.getName() + " in class " + commandsClassInstance.getClass().getName() + ". Required argument: fr.olympa.api.command.complex.CommandContext");
			}
		}
	}

}