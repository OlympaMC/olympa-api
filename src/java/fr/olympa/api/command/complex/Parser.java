package fr.olympa.api.command.complex;

public class Parser<T> implements IArgument {

	private CommandArgsType type;
	private ArgumentParser<T> argumentParser;

	public Parser(ArgumentParser<T> argumentParser) {
		this.argumentParser = argumentParser;
	}

	@Override
	public CommandArgsType getType() {
		return type;
	}

	public ArgumentParser<T> getArgumentParser() {
		return argumentParser;
	}

	@Override
	public void setType(CommandArgsType type) {
		this.type = type;
	}

}
