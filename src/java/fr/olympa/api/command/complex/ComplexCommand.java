package fr.olympa.api.command.complex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.Multimap;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.player.OlympaPlayerInformations;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.DivideList;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.Utils;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.ChatColor;

public class ComplexCommand extends OlympaCommand implements IComplexCommand<CommandSender> {

	public class SpigotInternalCommand extends InternalCommand {

		SpigotInternalCommand(Cmd cmd, Method method, Object commandsClass) {
			super(cmd, method, commandsClass);
		}

		@Override
		public boolean canRun() {
			return hasPermission(perm) && (!cmd.player() || !isConsole());
		}

		boolean canRun(CommandSender sender) {
			return sender instanceof Player ? hasPermission(perm, AccountProvider.get(((Player) sender).getUniqueId())) : !cmd.player();
		}
	}

	@Override
	public void addArgumentParser(String name, ArgumentParser<CommandSender> parser) {
		parsers.put(name, parser);
	}

	public final Map<List<String>, InternalCommand> commands = new HashMap<>();
	private final Map<String, ArgumentParser<CommandSender>> parsers = new HashMap<>();

	@SuppressWarnings("deprecation")
	public ComplexCommand(Plugin plugin, String command, String description, OlympaSpigotPermission permission, String... aliases) {
		super(plugin, command, description, permission, aliases);
		addDefaultParsers();

		BiFunction<CommandSender, String, Collection<String>> offlinePlayers = (sender, arg) -> arg.length() > 1
				? Stream.concat(Arrays.stream(plugin.getServer().getOfflinePlayers()).map(OfflinePlayer::getName),
						AccountProvider.getAllPlayersInformations().stream().map(OlympaPlayerInformations::getName)).collect(Collectors.toList())
				: Collections.emptyList();
		Server server = plugin.getServer();
		addArgumentParser("OLYMPA_PLAYERS", offlinePlayers, x -> {
			try {
				return AccountProvider.get(x);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}, x -> String.format("Le joueur &4%s&c est introuvable.", x));
		addArgumentParser("OLYMPA_PLAYERS_INFO", offlinePlayers, x -> {
			return AccountProvider.getPlayerInformations(x);
		}, x -> String.format("Le joueur &4%s&c est introuvable.", x));
		addArgumentParser("OFFLINE_PLAYERS", offlinePlayers, x -> {
			OfflinePlayer p = server.getOfflinePlayer(x);
			//if (p.hasPlayedBefore())
			return p;
		}, x -> String.format("Le joueur &4%s&c est introuvable", x));
		addArgumentParser("PLAYERS", (sender, arg) -> server.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()), x -> {
			return server.getPlayerExact(x);
		}, x -> String.format("Le joueur &4%s&c est introuvable", x));
		addArgumentParser("SUBCOMMAND", (sender, arg) -> commands.entrySet().stream().filter(e -> !e.getValue().cmd.otherArg() && !e.getValue().cmd.hide()).map(Entry::getKey).flatMap(List::stream).collect(Collectors.toList()), x -> {
			InternalCommand result = getCommand(x);
			if (result != null && result.cmd.otherArg())
				return null;
			return result;
		}, x -> String.format("La commande &4%s&c n'existe pas", x));
		addArgumentParser("WORLD", (sender, arg) -> server.getWorlds().stream().map(World::getName).collect(Collectors.toList()), x -> {
			return server.getWorld(x);
		}, x -> String.format("Le monde &4%s&c n'existe pas", x));
		CommandArgsType.PARSER.setDetectType(s -> parsers.get(s.endsWith("...") ? s.substring(0, s.length() - 3) : s) != null);
		registerCommandsClass(this);
	}

	@Override
	public boolean containsCommand(String argName) {
		return commands.entrySet().stream().anyMatch(entry -> entry.getKey().contains(argName.toLowerCase()) || entry.getValue().cmd.otherArg());
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		return false;
	}

	/*
	 * Work in progress
	 */
	private final static Map<String, Parser<CommandSender>> parsers2 = new HashMap<>();
	private Multimap<Integer, IArgument> listOfArgs;

	public void creatArgs(int index, String cmdArg) {
		if (cmdArg == null || cmdArg.isBlank())
			return;
		String[] types = cmdArg.split("\\|");
		for (String type : types) {
			IArgument iArg = CommandArgsType.extractEntry(type, parsers2);
			if (iArg == null)
				new IllegalAccessError("L'argument renseigné dans @Cmd du ComplexCommand n'est pas correct, '" + types + "' doit être tous en lower case si c'est un string classic ou en majucule si il faut partit d'une liste. ")
						.printStackTrace();
			else
				listOfArgs.put(index, iArg);
		}
	}

	public void addParser(String s, ArgumentParser<CommandSender> p) {
		if (!parsers2.containsKey(s))
			parsers2.put(s, new Parser<>(p));
	}

	//	@Override
	public OlympaCommand register2() {
		OlympaCommand reg = register();
		try {
			parsers.entrySet().forEach(entry -> addParser(entry.getKey(), entry.getValue()));
			getCommands().forEach((aliases, internal) -> {
				for (int i = 0; i < internal.cmd.args().length; i++)
					creatArgs(i, internal.cmd.args()[i]);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
		return reg;
	}
	/*
	 *
	 */

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] rawArgs) {
		if (rawArgs.length == 0) {
			if (!noArguments(sender))
				sendError("Syntaxe incorrecte. Essaye &4/%s help&c.", label);
			return true;
		}

		String methodName = rawArgs[0];
		InternalCommand internal = getCommand(methodName);
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
		if (cmd.otherArg()) {
			methodName = null;
			minArg--;
		}
		if (rawArgs.length - 1 < minArg) {
			if ("".equals(cmd.syntax()))
				this.sendIncorrectSyntax();
			else
				this.sendIncorrectSyntax("/" + label + (methodName != null ? " " + methodName : "") + " " + cmd.syntax());
			return true;
		}

		int rawArgIndex = cmd.otherArg() ? 0 : 1;
		Object[] newArgs = new Object[rawArgs.length - rawArgIndex];

		boolean canUseColors = hasPermission(OlympaAPIPermissions.ARG_COLOR);
		for (int newArgIndex = 0; newArgIndex < newArgs.length; newArgIndex++) {
			String arg;
			if (canUseColors)
				arg = ChatColor.translateAlternateColorCodes('&', rawArgs[rawArgIndex++]);
			else
				arg = rawArgs[rawArgIndex++];
			String[] types = newArgIndex >= cmd.args().length ? new String[0] : cmd.args()[newArgIndex].split("\\|");
			if (types.length == 1 && cmd.args()[newArgIndex].contains(" ") && newArgIndex < newArgs.length - 1 && cmd.args()[newArgIndex].equalsIgnoreCase(buildText(newArgIndex, rawArgs))) {
				sendMessage(Prefix.DEFAULT_BAD, "\"&4%s&c\" est une indication voyons, ne l'ajoute pas dans la commande !", cmd.args()[newArgIndex]);
				return true;
			}
			Object result = null;
			DivideList<String> divideList = new DivideList<>(Arrays.asList(types), sType -> parsers.entrySet().stream().anyMatch(t -> sType.equals(t.getKey()))).divide();
			List<ArgumentParser<CommandSender>> potentialParsers = divideList.getTrue().stream().map(sType -> parsers.get(sType)).collect(Collectors.toList());
			List<String> potentialString = divideList.getFalse();
			List<String> potentialStringUpper = potentialString.stream().filter(s -> Utils.isAllUpperCase(s)).collect(Collectors.toList());
			ArgumentParser<CommandSender> parser = potentialParsers.stream().filter(p -> p.applyTab(sender, arg).contains(arg)).findFirst().orElse(null);
			if (parser != null)
				result = parser.supplyArgumentFunction.apply(arg);
			else
				// TODO : Choose between 2 parses here
				result = potentialParsers.stream().map(p -> p.supplyArgumentFunction.apply(arg)).filter(r -> r != null).findFirst().orElse(null);
			if (!potentialString.isEmpty() && result == null && (potentialStringUpper.isEmpty() || potentialStringUpper.contains(arg.toUpperCase()) || types.length == 0))
				result = arg;
			if (result == null) {
				if ("".equals(cmd.syntax()) && potentialParsers.isEmpty())
					this.sendIncorrectSyntax(internal);
				else if (!potentialParsers.isEmpty()) {
					sendError("%s.", potentialParsers.stream().filter(e -> e.wrongArgTypeMessageFunction != null)
							.map(e -> e.wrongArgTypeMessageFunction.apply(arg).replaceFirst("\\.$", ""))
							.collect(Collectors.joining(" &4&lou&c ")));
					if (potentialParsers.size() <= 1)
						return true;
				} else
					this.sendIncorrectSyntax("/" + label + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
				return true;
			}
			newArgs[newArgIndex] = result;
		}
		try {
			internal.method.invoke(internal.commands, new CommandContext(sender, this, newArgs, label, methodName));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sendError("Une erreur est survenue.");
			e.printStackTrace();
		}

		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
		List<String> tmp = new ArrayList<>();
		List<String> find;
		String sel = args[0];
		if (args.length == 1) {
			find = new ArrayList<>();
			for (Entry<List<String>, InternalCommand> en : commands.entrySet())
				if (en.getValue().cmd.otherArg())
					find.addAll(findPotentialArgs(sender, args));
				else if (!en.getValue().cmd.hide() && en.getValue().canRun())
					if (en.getValue().cmd.registerAliasesInTab())
						find.addAll(en.getKey());
					else
						find.add(en.getKey().get(0));
		} else if (args.length >= 2) {
			find = findPotentialArgs(sender, args);
			sel = args[args.length - 1];
		} else
			return tmp;
		if (sel.isBlank())
			return find;
		for (String arg : find)
			if (arg.toLowerCase().startsWith(sel.toLowerCase()))
				tmp.add(arg);
		return tmp;
	}

	@Override
	public void registerCommandsClass(Class<?> clazz, Object commandsClassInstance) {
		for (Method method : clazz.getDeclaredMethods())
			if (method.isAnnotationPresent(Cmd.class)) {
				Cmd cmd = method.getDeclaredAnnotation(Cmd.class);
				if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == CommandContext.class) {
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
		sender.spigot().sendMessage(helpExtra().build());
	}

	@Override
	@Cmd(args = "SUBCOMMAND", syntax = "[commande]", description = "Affiche l'aide de la commande")
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
			sender.spigot().sendMessage(getHelpCommandComponent(command).build());
		}
	}

	@Override
	public void sendIncorrectSyntax(InternalCommand internal) {
		sendIncorrectSyntax(internal.cmd.syntax());
	}

	@Override
	public TxtComponentBuilder getHelpCommandComponent(InternalCommand command) {
		return getHelpCommandComponent(this.command, command);
	}

	@Override
	public Map<List<String>, InternalCommand> getCommands() {
		return commands;
	}

	@Override
	public Map<String, ArgumentParser<CommandSender>> getParsers() {
		return parsers;
	}

	public static Map<String, Parser<CommandSender>> getParsers2() {
		return parsers2;
	}

}