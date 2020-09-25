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
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.match.RegexMatcher;
import fr.olympa.api.permission.OlympaPermission;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

public class ComplexCommand extends OlympaCommand implements IComplexCommand {

	public class SpigotInternalCommand {
		public Cmd cmd;
		public OlympaPermission perm;
		public Method method;
		public Object commands;
		public String name;

		SpigotInternalCommand(Cmd cmd, Method method, Object commandsClass) {
			this.cmd = cmd;
			this.method = method;
			commands = commandsClass;
			perm = OlympaPermission.permissions.get(cmd.permissionName());
			name = method.getName();
		}

		boolean canRun() {
			return cmd.hide() || (hasPermission(perm) && (!cmd.player() || !isConsole()));
		}
	}

	public class SpigotArgumentParser extends ArgumentParser {

		public Function<CommandSender, List<String>> tabArgumentsFunction;

		/**
		 * @Deprecated
		 *
		 * Il est possible d'utiliser le tabArgumentsFunction pour vérifier le type de l'arguement.
		 * Il suffit de mettre le message d'erreur dans errorMessageArgumentFunction plutôt que dans tabArgumentsFunction sinon le message d'erreur sera envoyé avec que le plugin le gère.
		 *
		 * Utilise plutôt {@link #ComplexUtils(tabArgumentsFunction, supplyArgumentFunction, errorMessageArgumentFunction) ComplexUtils}.
		 */
		@Deprecated(forRemoval = true)
		public SpigotArgumentParser(Function<CommandSender, List<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction) {
			super(supplyArgumentFunction);
			this.tabArgumentsFunction = tabArgumentsFunction;
		}

		/**
		 *
		 * @param tabArgumentsFunction
		 * @param supplyArgumentFunction
		 * @param wrongArgTypeMessageFunction Le message ne doit pas finir par un point, et doit avoir un sens en utiliser le message suivie d'un ou (ex: Ton message d'erreur OU un autre message d'erreur)
		 */
		public SpigotArgumentParser(Function<CommandSender, List<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, Function<String, String> wrongArgTypeMessageFunction) {
			super(supplyArgumentFunction, wrongArgTypeMessageFunction);
			this.tabArgumentsFunction = tabArgumentsFunction;
		}

	}

	@Override
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

	@Deprecated(forRemoval = true)
	public void addArgumentParser(String name, Function<CommandSender, List<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction) {
		parsers.put(name, new SpigotArgumentParser(tabArgumentsFunction, supplyArgumentFunction));
	}

	public void addArgumentParser(String name, Function<CommandSender, List<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, Function<String, String> errorMessageArgumentFunction) {
		parsers.put(name, new SpigotArgumentParser(tabArgumentsFunction, supplyArgumentFunction, errorMessageArgumentFunction));
	}

	protected static final HoverEvent COMMAND_HOVER = new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§bSuggérer la commande."));
	protected static final List<String> INTEGERS = Arrays.asList("1", "2", "3");
	private static final String uuid = UUID.randomUUID().toString();
	protected static final List<String> UUIDS = Arrays.asList(uuid, uuid.replace("-", ""));
	protected static final List<String> BOOLEAN = Arrays.asList("true", "false");
	public final Map<List<String>, SpigotInternalCommand> commands = new HashMap<>();
	private final Map<String, SpigotArgumentParser> parsers = new HashMap<>();

	public ComplexCommand(Plugin plugin, String command, String description, OlympaPermission permission, String... aliases) {
		super(plugin, command, description, permission, aliases);
		addArgumentParser("INTEGER", sender -> INTEGERS, x -> {
			if (RegexMatcher.INT.is(x))
				return RegexMatcher.INT.parse(x);
			return null;
		}, x -> String.format("&4%s&c doit être un nombre entier", x));
		addArgumentParser("UUID", sender -> UUIDS, x -> {
			if (RegexMatcher.UUID.is(x))
				return RegexMatcher.UUID.parse(x);
			return null;
		}, x -> {
			String random = UUID.randomUUID().toString();
			return String.format("&4%s&c doit être un uuid sous la forme &4%s&c ou &4%s&c", x, random, random.replace("-", ""));
		});
		addArgumentParser("DOUBLE", sender -> Collections.EMPTY_LIST, x -> {
			if (RegexMatcher.DOUBLE.is(x))
				return RegexMatcher.DOUBLE.parse(x);
			return null;
		}, x -> String.format("&4%s&c doit être un nombre décimal", x));
		addArgumentParser("BOOLEAN", sender -> BOOLEAN, Boolean::parseBoolean);
		addArgumentParser("SUBCOMMAND", sender -> commands.entrySet().stream().filter(e -> !e.getValue().cmd.otherArg()).map(Entry::getKey).flatMap(List::stream).collect(Collectors.toList()), x -> {
			SpigotInternalCommand result = getCommand(x);
			if (result != null && result.cmd.otherArg())
				return null;
			return result;
		}, x -> String.format("La commande &4%s&c n'existe pas", x));
		registerCommandsClass(this);
	}

	public SpigotInternalCommand getCommand(String argName) {
		return commands.entrySet().stream().filter(entry -> entry.getKey().contains(argName.toLowerCase())).findFirst().map(entry -> entry.getValue())
				.orElse(commands.entrySet().stream().filter(entry -> entry.getValue().cmd.otherArg()).map(entry -> entry.getValue()).findFirst().orElse(null));
	}

	@Override
	public boolean containsCommand(String argName) {
		return commands.entrySet().stream().anyMatch(entry -> entry.getKey().contains(argName.toLowerCase()) || entry.getValue().cmd.otherArg());
	}

	public boolean noArguments(CommandSender sender) {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length == 0) {
			if (!noArguments(sender))
				sendError("Syntaxe incorrecte. Essaye &4/%s help&c.", label);
			return true;
		}

		SpigotInternalCommand internal = getCommand(args[0]);
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

		int minArg = cmd.min();
		if (cmd.otherArg())
			minArg--;
		if (args.length - 1 < minArg) {
			if ("".equals(cmd.syntax()))
				this.sendIncorrectSyntax();
			this.sendIncorrectSyntax("/" + label + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
			return true;
		}

		int i1 = 1;
		if (cmd.otherArg())
			i1 = 0;
		Object[] argsCmd = new Object[args.length - i1];
		for (int i2 = 0; i2 < argsCmd.length; i2++) {
			String arg = args[i1++];
			String[] types = (i2 >= cmd.args().length ? "" : cmd.args()[i2]).split("\\|");
			Object result = null;
			List<SpigotArgumentParser> potentialParsers = parsers.entrySet().stream().filter(entry -> Arrays.stream(types).anyMatch(type -> entry.getKey().equals(type)))
					.map(Entry::getValue).collect(Collectors.toList());
			boolean hasStringType = potentialParsers.size() != types.length;
			if (potentialParsers.isEmpty())
				result = arg;
			else {
				SpigotArgumentParser parser = potentialParsers.stream().filter(p -> p.tabArgumentsFunction.apply(sender).contains(arg)).findFirst().orElse(null);
				if (parser != null)
					result = parser.supplyArgumentFunction.apply(arg);
				else
					// TODO : Choose between 2 parses here
					for (SpigotArgumentParser p : potentialParsers) {
						result = p.supplyArgumentFunction.apply(arg);
						if (result != null)
							break;
					}
				if (result == null && !hasStringType) {
					if ("".equals(cmd.syntax()) && potentialParsers.isEmpty()) {
						this.sendIncorrectSyntax();
						return true;
					} else if (!potentialParsers.isEmpty()) {
						sendError("%s.", potentialParsers.stream().filter(e -> e.wrongArgTypeMessageFunction != null)
								.map(e -> e.wrongArgTypeMessageFunction.apply(arg).replaceFirst("\\.$", ""))
								.collect(Collectors.joining(" &4&lou&c ")));
						if (potentialParsers.size() <= 1)
							return true;
					}
					this.sendIncorrectSyntax("/" + label + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
					return true;
				}
			}

			argsCmd[i2] = result;
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
		if (args.length == 1) {
			for (Entry<List<String>, SpigotInternalCommand> en : commands.entrySet())
				if (en.getValue().cmd.otherArg())
					find.addAll(findPotentialArgs(args));
				else if (!en.getValue().cmd.hide() || en.getValue().canRun())
					find.add(en.getKey().get(0));
		} else if (args.length >= 2) {
			find = findPotentialArgs(args);
			sel = args[args.length - 1];
		} else
			return tmp;
		for (String arg : find)
			if (arg.toLowerCase().startsWith(sel.toLowerCase()))
				tmp.add(arg);
		return tmp;
	}

	private List<String> findPotentialArgs(String[] args) {
		List<String> find = new ArrayList<>();
		int index = args.length - 2;
		String sel = args[0];
		if (!containsCommand(sel))
			return find;
		SpigotInternalCommand internal = getCommand(sel);
		String[] needed = internal.cmd.args();
		if (internal.cmd.otherArg())
			index++;
		else if (args.length == 1)
			return find;
		if (needed.length <= index || !internal.cmd.permissionName().isEmpty() && !internal.perm.hasSenderPermission(sender))
			return find;
		String[] types = needed[index].split("\\|");
		for (String type : types) {
			SpigotArgumentParser parser = parsers.get(type);
			if (parser != null)
				find.addAll(parser.tabArgumentsFunction.apply(sender));
			else
				find.add(type);
		}
		return find;
	}

	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	@Override
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
						commands.put(argNames, new SpigotInternalCommand(cmd, method, commandsClassInstance));
						continue;
					}
				OlympaCore.getInstance()
						.sendMessage("Error when loading command annotated method " + method.getName() + " in class " + method.getDeclaringClass().getName() + ". Required argument: fr.olympa.api.command.complex.CommandContext");
			}
	}

	@Override
	public void sendHelp(CommandSender sender) {
		super.sendHelp(sender);
		for (SpigotInternalCommand command : commands.values()) {
			if (!command.canRun())
				continue;
			sender.spigot().sendMessage(getHelpCommandComponent(command));
		}
	}

	@Override
	@Cmd(args = "SUBCOMMAND", syntax = "[commande]")
	public void help(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			sendHelp(sender);
		else {
			if (!(cmd.getArgument(0) instanceof SpigotInternalCommand)) {
				sendIncorrectSyntax();
				return;
			}
			SpigotInternalCommand command = cmd.getArgument(0);
			if (!command.canRun()) {
				sendIncorrectSyntax();
				return;
			}
			sender.spigot().sendMessage(getHelpCommandComponent(command));
		}
	}

	private TextComponent getHelpCommandComponent(SpigotInternalCommand command) {
		String fullCommand;
		if (!command.cmd.otherArg())
			fullCommand = "/" + this.command + " " + command.name;
		else
			fullCommand = "/" + this.command;
		TextComponent component = new TextComponent();
		component.setHoverEvent(COMMAND_HOVER);
		component.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, fullCommand + " "));

		for (BaseComponent commandCompo : TextComponent.fromLegacyText("§7➤ §6" + fullCommand + " §e" + command.cmd.syntax()))
			component.addExtra(commandCompo);

		return component;
	}

}