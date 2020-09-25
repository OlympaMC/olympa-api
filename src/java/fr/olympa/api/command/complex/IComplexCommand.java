package fr.olympa.api.command.complex;

public interface IComplexCommand {

	boolean containsCommand(String argName);

	<T extends Enum<T>> void addArgumentParser(String name, Class<T> enumClass);

	/**
	 * Register all available commands from an instance of a Class
	 * @param commandsClassInstance Instance of the Class
	 */
	void registerCommandsClass(Object commandsClassInstance);

	void help(CommandContext cmd);

}