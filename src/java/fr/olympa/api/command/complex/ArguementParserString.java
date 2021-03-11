package fr.olympa.api.command.complex;

public class ArguementParserString implements IArgument {

	private String string;

	/**
	 * @param type
	 * @param string
	 */
	public ArguementParserString(String string) {
		this.string = string;
	}

	public String getString() {
		return string;
	}

	public boolean match(String s) {
		return string.equals(s);
	}

}
