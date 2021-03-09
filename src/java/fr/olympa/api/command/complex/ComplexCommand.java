package fr.olympa.api.command.complex;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.command.OlympaCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.permission.OlympaSpigotPermission;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
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
		BiFunction<CommandSender, String, Collection<String>> offlinePlayers = (sender, arg) -> Arrays.stream(plugin.getServer().getOfflinePlayers()).map(OfflinePlayer::getName).collect(Collectors.toList());
		Server server = plugin.getServer();
		addArgumentParser("OLYMPA_PLAYER", offlinePlayers, x -> {
			try {
				return AccountProvider.get(x);
			} catch (SQLException e) {
				e.printStackTrace();
				return null;
			}
		}, x -> String.format("Le joueur &4%s&c est introuvable", x));
		addArgumentParser("OFFLINE_PLAYERS", offlinePlayers, x -> {
			return server.getOfflinePlayer(x);
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

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] rawArgs) {
		if (rawArgs.length == 0) {
			if (!noArguments(sender))
				sendError("Syntaxe incorrecte. Essaye &4/%s help&c.", label);
			return true;
		}

		InternalCommand internal = getCommand(rawArgs[0]);
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
		if (rawArgs.length - 1 < minArg) {
			if ("".equals(cmd.syntax()))
				this.sendIncorrectSyntax();
			else
				this.sendIncorrectSyntax("/" + label + " " + (!cmd.otherArg() ? internal.method.getName() : "") + " " + cmd.syntax());
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
			if (types.length == 1 && cmd.args()[newArgIndex].contains(" ")) {
				System.out.println("DEBUG ComplexCommand > cmd.args()[newArgIndex] " + cmd.args()[newArgIndex] + " arg " + arg);
				if (newArgIndex < newArgs.length - 1 && cmd.args()[newArgIndex].equalsIgnoreCase(buildText(newArgIndex, rawArgs)))
					sendMessage(Prefix.DEFAULT_BAD, "\"&4%s&c\" est une indication voyons, ne l'ajoute pas dans la commande !", cmd.args()[newArgIndex]);
				return true;
			}
			Object result = null;
			List<ArgumentParser<CommandSender>> potentialParsers = parsers.entrySet().stream().filter(entry -> Arrays.stream(types).anyMatch(type -> entry.getKey().equals(type)))
					.map(Entry::getValue).collect(Collectors.toList());
			boolean hasStringType = potentialParsers.size() != types.length;
			if (potentialParsers.isEmpty())
				result = arg;
			else {
				//				List<ArgumentParser<CommandSender>> parsers = potentialParsers.stream().filter(p -> p.tabArgumentsFunction.apply(sender).contains(arg)).collect(Collectors.toList());
				List<ArgumentParser<CommandSender>> parsers = potentialParsers.stream().filter(p -> p.cache != null ? p.applyTabWithoutCache(sender, arg).contains(arg) : true).collect(Collectors.toList());
				if (!parsers.isEmpty())
					result = parsers.get(0).supplyArgumentFunction.apply(arg);
				else {
					// TODO : Choose between 2 parses here
					//					ISender isender = ISender.of(sender, null);
					//					AwaitResponse<?> response = new AwaitResponse<String>(isender, "&m----------------", "&m----------------", "&4Impossible de déterminer le type d'argument.", "&eTu dois choisir entre deux type d'argument");
					//					for (SpigotArgumentParser p : parsers) {
					//						Object r = p.supplyArgumentFunction.apply(arg);
					//						response.choices(new ClickChoice(p, null, p.));
					//					}
					//					ReponseEvent.add(ISender.of(sender, null), response);
				}
				//				else
				//					for (SpigotArgumentParser p : potentialParsers) {
				//						result = p.supplyArgumentFunction.apply(arg);
				//						if (result != null)
				//							break;
				//					}
				if (result == null && !hasStringType) {
					if ("".equals(cmd.syntax()) && potentialParsers.isEmpty()) {
						this.sendIncorrectSyntax(internal);
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

			newArgs[newArgIndex] = result;
		}

		try

		{
			internal.method.invoke(internal.commands, new CommandContext(sender, this, newArgs, label));
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

}