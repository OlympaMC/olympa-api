package fr.olympa.api.command.complex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.chat.TxtComponentBuilder;
import fr.olympa.api.match.RegexMatcher;

public interface IComplexCommand<C> {

	List<String> INTEGERS = Arrays.asList("1", "2", "3");
	String TEMP_UUID = UUID.randomUUID().toString();
	List<String> UUIDS = Arrays.asList(TEMP_UUID, TEMP_UUID.replace("-", ""));
	List<String> BOOLEAN = Arrays.asList("true", "false");
	List<String> HEX_COLOR = Arrays.asList("#123456", "#FFFFFF");
	List<String> IP = Arrays.asList("127.0.0.1");

	default void addDefaultParsers() {
		addArgumentParser("INTEGER", (sender, arg) -> INTEGERS, x -> {
			if (RegexMatcher.INT.is(x))
				return RegexMatcher.INT.parse(x);
			return null;
		}, x -> String.format("&4%s&c doit être un nombre entier", x), false);
		addArgumentParser("UUID", (sender, arg) -> UUIDS, x -> {
			if (RegexMatcher.UUID.is(x))
				return RegexMatcher.UUID.parse(x);
			return null;
		}, x -> {
			String random = UUID.randomUUID().toString();
			return String.format("&4%s&c doit être un uuid sous la forme &4%s&c ou &4%s&c", x, random, random.replace("-", ""));
		}, false);
		addArgumentParser("DOUBLE", (sender, arg) -> Collections.emptyList(), x -> {
			if (RegexMatcher.DOUBLE.is(x))
				return RegexMatcher.DOUBLE.parse(x);
			return null;
		}, x -> String.format("&4%s&c doit être un nombre décimal", x), false);
		addArgumentParser("HEX_COLOR", (sender, arg) -> HEX_COLOR, x -> {
			if (RegexMatcher.HEX_COLOR.is(x))
				return RegexMatcher.HEX_COLOR.parse(x);
			return null;
		}, x -> String.format("&4%s&c n'est pas un code hexadicimal sous la forme &4#123456&c ou &4#FFFFFF&c.", x), false);
		addArgumentParser("BOOLEAN", (sender, arg) -> BOOLEAN, Boolean::parseBoolean, null);
		addArgumentParser("IP", (sender, arg) -> IP, x -> {
			if (RegexMatcher.IP.is(x))
				return RegexMatcher.IP.parse(x);
			return null;
		}, x -> String.format("&4%s&c n'est pas une IPv4 sous la forme &4%s&c.", x, ColorUtils.joinRedOu(IP)), false);
	}

	default <T extends Enum<T>> void addArgumentParser(String name, Class<T> enumClass) {
		List<String> values = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(Collectors.toList());
		addArgumentParser(name, (sender, arg) -> values, playerInput -> {
			for (T each : enumClass.getEnumConstants())
				if (each.name().equalsIgnoreCase(playerInput))
					return each;
			return null;
		}, x -> String.format("La valeur &4%s&c n'existe pas.", x));
	}

	default <T extends Enum<T>> void addArgumentParser(String name, Class<T> enumClass, Function<? super T, ? extends String> getNameMethod) {
		List<String> values = Arrays.stream(enumClass.getEnumConstants()).map(getNameMethod).collect(Collectors.toList());
		addArgumentParser(name, (sender, arg) -> values, playerInput -> {
			for (T each : enumClass.getEnumConstants())
				if (each.name().equalsIgnoreCase(playerInput))
					return each;
			return null;
		}, x -> String.format("La valeur &4%s&c n'existe pas.", x));
	}

	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	default void registerCommandsClass(Object commandsClassInstance) {
		Class<?> clazz = commandsClassInstance.getClass();
		do
			registerCommandsClass(clazz, commandsClassInstance);
		while ((clazz = clazz.getSuperclass()) != null);
	}

	void registerCommandsClass(Class<?> clazz, Object commandsClassInstance);

	void addArgumentParser(String name, ArgumentParser<C> parser);

	boolean noArguments(C sender);

	default void addArgumentParser(String name, BiFunction<C, String, Collection<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, UnaryOperator<String> errorMessageArgumentFunction) {
		addArgumentParser(name, new ArgumentParser<>(tabArgumentsFunction, supplyArgumentFunction, errorMessageArgumentFunction));
	}

	default void addArgumentParser(String name, BiFunction<C, String, Collection<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, UnaryOperator<String> errorMessageArgumentFunction, boolean hasCache) {
		addArgumentParser(name, new ArgumentParser<>(tabArgumentsFunction, supplyArgumentFunction, errorMessageArgumentFunction, hasCache));
	}

	@Deprecated(forRemoval = true)
	default void addArgumentParser(String name, Function<C, Collection<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, UnaryOperator<String> errorMessageArgumentFunction) {
		addArgumentParser(name, new ArgumentParser<>(tabArgumentsFunction, supplyArgumentFunction, errorMessageArgumentFunction));
	}

	default InternalCommand getCommand(String argName) {
		return getCommands().entrySet().stream().filter(entry -> entry.getKey().contains(argName.toLowerCase())).findFirst().map(Entry::getValue)
				.orElse(getCommands().entrySet().stream().filter(entry -> entry.getValue().cmd.otherArg()).map(Entry::getValue).findFirst().orElse(null));
	}

	Map<List<String>, InternalCommand> getCommands();

	Map<String, ArgumentParser<C>> getParsers();

	boolean containsCommand(String argName);

	void sendIncorrectSyntax(InternalCommand internal);

	void help(CommandContext cmd);

	TxtComponentBuilder getHelpCommandComponent(InternalCommand command);

	default TxtComponentBuilder helpExtra() {
		TxtComponentBuilder txt = new TxtComponentBuilder().extraSpliterBN();
		for (InternalCommand command : getCommands().values()) {
			if (command.cmd.hide() || !command.canRun())
				continue;
			txt.extra(getHelpCommandComponent(command));
		}
		return txt;
	}

	default TxtComponentBuilder getHelpCommandComponent(String cmd, InternalCommand command) {
		TxtComponentBuilder builder = new TxtComponentBuilder().extraSpliterBN();
		if (command.cmd.registerAliasesInTab() && command.cmd.aliases() != null) {
			for (String aliase : command.cmd.aliases())
				builder.extra(getHelpCommandComponent(cmd, command, aliase));
		}else builder.extra(getHelpCommandComponent(cmd, command, command.name));
		return builder;
	}

	default TxtComponentBuilder getHelpCommandComponent(String cmd, InternalCommand command, String subCommand) {
		String fullCommand;
		if (subCommand != null && !subCommand.isBlank())
			fullCommand = "/" + cmd + " " + subCommand;
		else
			fullCommand = "/" + cmd;
		String description;
		if (command.cmd.description().isBlank())
			description = "§aClique pour suggérer la commande.";
		else
			description = "&a" + command.cmd.description();
		return new TxtComponentBuilder("&7➤ &6%s &e%s", fullCommand, command.cmd.syntax()).onHoverText(description).onClickSuggest(fullCommand + " ");
	}

	default List<String> findPotentialArgs(C sender, String[] args) {
		List<String> find = new ArrayList<>();
		int index = args.length - 2;
		String sel = args[0];
		if (!containsCommand(sel))
			return find;
		InternalCommand internal = getCommand(sel);
		String[] needed = internal.cmd.args();
		if (internal.cmd.otherArg())
			index++;
		else if (args.length == 1)
			return find;
		if (needed.length <= index || !internal.cmd.permissionName().isEmpty() && !internal.canRun())
			return find;
		String[] types = needed[index].split("\\|");
		for (String type : types) {
			ArgumentParser<C> parser = getParsers().get(type);
			if (parser != null)
				//				find.addAll(parser.tabArgumentsFunction.apply(sender));
				find.addAll(parser.applyTab(sender, args[args.length - 1]));
			else
				find.add(type);
		}
		return find;
	}

}