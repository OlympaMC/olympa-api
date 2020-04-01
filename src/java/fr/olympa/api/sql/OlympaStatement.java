package fr.olympa.api.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;

public class OlympaStatement {

	private final String statement;
	private boolean returnGeneratedKeys;

	public OlympaStatement(String statement) {
		this(statement, false);
	}

	public OlympaStatement(String statement, boolean returnGeneratedKeys) {
		this.statement = statement;
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	private PreparedStatement prepared;
	public PreparedStatement getStatement() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public String getStatementCommand() {
		return statement;
	}

}
