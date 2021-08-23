package fr.olympa.api.common.sql.statement;

public class OlympaStatementBuilder implements Cloneable {

	IStatementType type;
	String tableName;
	String[] what;
	String[] or;
	String orderCollumn;
	Boolean asc;
	int offset;
	int limit;
	String[] keys;
	private String statementCommand;
	private boolean returnGeneratedKeys = false;

	public OlympaStatementBuilder type(IStatementType type) {
		this.type = type;
		return this;
	}

	public OlympaStatementBuilder tableName(String tableName) {
		this.tableName = tableName;
		return this;
	}

	public OlympaStatementBuilder what(String[] what) {
		this.what = what;
		return this;
	}

	public OlympaStatementBuilder or(String[] or) {
		this.or = or;
		return this;
	}

	public OlympaStatementBuilder orderCollumn(String orderCollumn) {
		this.orderCollumn = orderCollumn;
		return this;
	}

	public OlympaStatementBuilder asc(Boolean asc) {
		this.asc = asc;
		return this;
	}

	public OlympaStatementBuilder offset(int offset) {
		this.offset = offset;
		return this;
	}

	public OlympaStatementBuilder limit(int limit) {
		this.limit = limit;
		return this;
	}

	public OlympaStatementBuilder keys(String[] keys) {
		this.keys = keys;
		return this;
	}

	public OlympaStatementBuilder statementCommand(String statementCommand) {
		this.statementCommand = statementCommand;
		return this;
	}

	public OlympaStatementBuilder returnGeneratedKeys(boolean returnGeneratedKeys) {
		this.returnGeneratedKeys = returnGeneratedKeys;
		return this;
	}

	public OlympaStatement build() {
		if (type instanceof StatementTypeSelectUpdate select)
			return new OlympaStatement(select, tableName, what, or, orderCollumn, asc, offset, limit, keys);
		else if (type instanceof StatementTypeInsertDelete insert)
			return new OlympaStatement(insert, tableName, keys);
		else if (type instanceof StatementTypeDefault def)
			return new OlympaStatement(def, tableName);
		return null;
	}



}
