package fr.olympa.api.command.complex;

public class ParserString implements IArgument {

	private String string;
	private CommandArgsType type;

	/**
	 * @param type
	 * @param string
	 */
	public ParserString(String string, CommandArgsType type) {
		this.string = string;
		this.type = type;
	}

	public String getString() {
		return string;
	}

	@Override
	public CommandArgsType getType() {
		return type;
	}

	@Override
	public void setType(CommandArgsType type) {
		this.type = type;
	}

}
