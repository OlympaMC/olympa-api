package fr.olympa.api.common.command.complex;

public interface IArgument {
	CommandArgsType getType();

	void setType(CommandArgsType type);
	//	boolean match(Object sender, String arg);
}
