package fr.olympa.api.bungee.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import fr.olympa.api.bungee.config.BungeeCustomConfig;
import fr.olympa.api.bungee.permission.OlympaBungeePermission;
import fr.olympa.api.common.chat.TxtComponentBuilder;
import fr.olympa.api.common.command.complex.ArgumentParser;
import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.command.complex.IComplexCommand;
import fr.olympa.api.common.command.complex.InternalCommand;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.core.spigot.OlympaCore;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public abstract class BungeeComplexCommand extends BungeeCommand implements IComplexCommand<CommandSender> {

	public class BungeeInternalCommand extends InternalCommand {

		public BungeeInternalCommand(Cmd cmd, Method method, Object commandsClass) {
			super(cmd, method, commandsClass);
		}

		@Override
		public boolean canRun() {
			return hasPermission(perm) && (!cmd.player() || !isConsole());
		}
	}

	public final Map<List<String>, InternalCommand> commands = new HashMap<>();
	private final Map<String, ArgumentParser<CommandSender>> parsers = new HashMap<>();

	public BungeeComplexCommand(Plugin plugin, String command, String description, OlympaBungeePermission permission, String... aliases) {
		super(plugin, command, description, permission, aliases);
		addDefaultParsers();
		addArgumentParser("PLAYERS", (sender, arg) -> plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()), x -> {
			return plugin.getProxy().getPlayer(x);
		}, x -> String.format("Le joueur &4%s&c est introuvable", x));
		addArgumentParser("OLYMPA_PLAYERS",
				(sender, arg) -> arg.length() > 2 ? AccountProviderAPI.getter().getSQL().getNamesBySimilarName(arg) : plugin.getProxy().getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()), x -> {
					try {
						return AccountProviderAPI.getter().get(x);
					} catch (SQLException e) {
						e.printStackTrace();
						return null;
					}
				}, x -> String.format("Le joueur &4%s&c ne s'est jamais connecté", x));
		addArgumentParser("SUBCOMMAND", (sender, arg) -> commands.entrySet().stream().filter(e -> !e.getValue().cmd.otherArg()).map(Entry::getKey).flatMap(List::stream).collect(Collectors.toList()), x -> {
			InternalCommand result = getCommand(x);
			if (result != null && result.cmd.otherArg())
				return null;
			return result;
		}, x -> String.format("La commande &4%s&c n'existe pas", x));
		addArgumentParser("CONFIGS", (sender, arg) -> BungeeCustomConfig.getConfigs().stream().map(BungeeCustomConfig::getName).collect(Collectors.toList()),
				x -> BungeeCustomConfig.getConfig(x),
				x -> String.format("La config &4%s&c n'existe pas", x));
		addArgumentParser("SERVERS", (sender, arg) -> plugin.getProxy().getServersCopy().keySet(),
				x -> plugin.getProxy().getServersCopy().get(x),
				x -> String.format("Le serveur &4%s&c n'existe pas", x));
		registerCommandsClass(this);
	}

	@Override
	public boolean containsCommand(String argName) {
		return commands.entrySet().stream().anyMatch(entry -> entry.getKey().contains(argName.toLowerCase()) || entry.getValue().cmd.otherArg());
	}

	@Override
	public void addArgumentParser(String name, ArgumentParser<CommandSender> parser) {
		parsers.put(name, parser);
	}

	@Override
	public boolean noArguments(CommandSender sender) {
		return false;
	}

	@Override
	public void onCommand(CommandSender sender, String[] args) {
		if (args.length == 0) {
			if (!noArguments(sender))
				sendError("Syntaxe incorrecte. Essaye &4/%s help&c.", command);
			return;
		}

		InternalCommand internal = getCommand(args[0]);
		if (internal == null) {
			sendError("La commande n'existe pas.");
			return;
		}

		Cmd cmd = internal.cmd;
		if (cmd.player() && !(sender instanceof ProxiedPlayer)) {
			sendImpossibleWithConsole();
			return;
		}
		if (!isConsole() && !hasPermission(internal.perm)) {
			sendDoNotHavePermission();
			return;
		}

		int minArg = cmd.min();
		if (cmd.otherArg())
			minArg--;
		if (args.length - 1 < minArg) {
			if ("".equals(cmd.syntax()))
				this.sendIncorrectSyntax();
			else
				this.sendIncorrectSyntax("/" + command + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
			return;
		}

		int i1 = 1;
		if (cmd.otherArg())
			i1 = 0;
		Object[] argsCmd = new Object[args.length - i1];
		for (int i2 = 0; i2 < argsCmd.length; i2++) {
			String arg = args[i1++];
			String[] types = (i2 >= cmd.args().length ? "" : cmd.args()[i2]).split("\\|");
			Object result = null;
			List<ArgumentParser<CommandSender>> potentialParsers = parsers.entrySet().stream().filter(entry -> Arrays.stream(types).anyMatch(type -> entry.getKey().equals(type)))
					.map(Entry::getValue).collect(Collectors.toList());
			boolean hasStringType = potentialParsers.size() != types.length;
			if (potentialParsers.isEmpty())
				result = arg;
			else {
				ArgumentParser<CommandSender> parser = potentialParsers.stream().filter(p -> p.applyTab(sender, arg).contains(arg)).findFirst().orElse(null);
				if (parser != null)
					result = parser.supplyArgumentFunction.apply(arg);
				else
					// TODO : Choose between 2 parses here
					for (ArgumentParser<CommandSender> p : potentialParsers) {
						result = p.supplyArgumentFunction.apply(arg);
						if (result != null)
							break;
					}
				if (result == null && !hasStringType) {
					if ("".equals(cmd.syntax()) && potentialParsers.isEmpty()) {
						this.sendIncorrectSyntax();
						return;
					} else if (!potentialParsers.isEmpty()) {
						sendError("%s.", potentialParsers.stream().filter(e -> e.wrongArgTypeMessageFunction != null)
								.map(e -> e.wrongArgTypeMessageFunction.apply(arg).replaceFirst("\\.$", ""))
								.collect(Collectors.joining(" &4&lou&c ")));
						if (potentialParsers.size() <= 1)
							return;
					}
					this.sendIncorrectSyntax("/" + command + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
					return;
				}
			}

			argsCmd[i2] = result;
		}

		try {
			internal.method.invoke(internal.commands, new CommandContext(sender, this, argsCmd, command, internal.method.getName()));
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			sendError("Une erreur est survenue.");
			e.printStackTrace();
		}
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, BungeeCommand command, String[] args) {
		List<String> tmp = new ArrayList<>();
		List<String> find = new ArrayList<>();
		String sel = args[0];
		if (args.length == 1) {
			for (Entry<List<String>, InternalCommand> en : commands.entrySet())
				if (en.getValue().cmd.otherArg())
					find.addAll(findPotentialArgs(sender, args));
				else if (!en.getValue().cmd.hide() || en.getValue().canRun())
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
				if (method.getParameterCount() == 1)
					if (method.getParameterTypes()[0] == CommandContext.class) {
						List<String> argNames = new ArrayList<>();
						argNames.add(method.getName().toLowerCase());
						if (cmd.aliases() != null)
							argNames.addAll(Arrays.asList(cmd.aliases()));
						commands.put(argNames, new BungeeInternalCommand(cmd, method, commandsClassInstance));
						continue;
					}
				OlympaCore.getInstance().sendMessage("Error when loading command annotated method " + method.getName() + " in class "
						+ method.getDeclaringClass().getName() + ". Required argument: fr.olympa.api.common.command.complex.CommandContext");
			}
	}

	@Override
	public void sendHelp(CommandSender sender) {
		super.sendHelp(sender);
		sender.sendMessage(helpExtra().build());
	}

	@Override
	@Cmd(args = "SUBCOMMAND", syntax = "[commande]")
	public void help(CommandContext cmd) {
		if (cmd.getArgumentsLength() == 0)
			sendHelp(sender);
		else {
			if (!(cmd.getArgument(0) instanceof BungeeInternalCommand)) {
				sendIncorrectSyntax();
				return;
			}
			BungeeInternalCommand command = cmd.getArgument(0);
			if (!command.canRun()) {
				sendIncorrectSyntax();
				return;
			}
			sender.sendMessage(getHelpCommandComponent(this.command, command).build());
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

}