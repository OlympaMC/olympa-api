package fr.olympa.api.command.complex;

public class CommandContext {

	public final ComplexCommand command;
	public final Object[] args;
	public final String label;
	
	public CommandContext(ComplexCommand command, Object[] args, String label) {
		this.command = command;
		this.args = args;
		this.label = label;
	}
	
	public <T> T getArgument(int id) {
		return (T) args[id];
	}
	
}