package fr.olympa.api.command.complex;

public class CommandContext {

	public final ComplexCommand command;
	public final String label;
	private final Object[] args;
	
	public CommandContext(ComplexCommand command, Object[] args, String label) {
		this.command = command;
		this.args = args;
		this.label = label;
	}
	
	public int getArgumentsLength() {
		return args.length;
	}

	public <T> T getArgument(int id) {
		return (T) args[id];
	}
	
	public <T> T getArgument(int id, T def) {
		return args.length < id ? def : (T) args[id];
	}

}