package fr.olympa.api.command.complex;

import java.util.StringJoiner;

import fr.olympa.api.chat.TableGenerator.Receiver;

@SuppressWarnings("unchecked")
public class CommandContext {

	public final Object sender;
	public final IComplexCommand<?> command;
	public final String label;
	public String methodName; // = aliases of cmd
	private final Object[] args;

	public CommandContext(Object sender, IComplexCommand<?> command, Object[] args, String label) {
		this.sender = sender;
		this.command = command;
		this.args = args;
		this.label = label;
	}

	public CommandContext(Object sender, IComplexCommand<?> command, Object[] args, String label, String methodName) {
		this.sender = sender;
		this.command = command;
		this.args = args;
		this.label = label;
		this.methodName = methodName;
	}

	public Receiver getSenderType() {
		return Receiver.of(sender);
	}

	public String getMethodName() {
		return methodName;
	}

	public boolean isAliase(String aliase) {
		return methodName.equalsIgnoreCase(aliase);
	}

	public int getArgumentsLength() {
		return args.length;
	}

	public <T> T getArgument(int id) {
		return (T) args[id];
	}

	public <T> T getArgument(int id, T def) {
		return id < args.length ? (T) args[id] : def;
	}

	public String getFrom(int arg) {
		if (arg >= args.length)
			throw new ArrayIndexOutOfBoundsException(arg);
		StringJoiner joiner = new StringJoiner(" ");
		for (int i = arg; i < args.length; i++)
			joiner.add(args[i].toString());
		return joiner.toString();
	}

}