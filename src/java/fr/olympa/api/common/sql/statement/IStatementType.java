package fr.olympa.api.common.sql.statement;

public abstract class IStatementType {

	String command;

	public IStatementType(String command) {
		this.command = command;
	}

	public String get() {
		return command;
	}

	public String getTypeName() {
		int index = command.indexOf(" ");
		return command.substring(0, index != -1 ? index : command.length());
	}

	public boolean isSame(IStatementType type2) {
		return get().equals(type2.get());
	}
}
