package fr.olympa.api.sql.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.StringJoiner;

import fr.olympa.api.LinkSpigotBungee;

public class OlympaStatement {

	public static String formatTableName(String tableName) {
		if (!tableName.startsWith("`"))
			if (tableName.contains(".")) {
				int i = tableName.indexOf(".");
				tableName = "`" + tableName.substring(0, i) + "`.`" + tableName.substring(i + 1, tableName.length()) + "`";
			} else
				tableName = "`" + tableName + "`";
		return tableName;
	}

	IStatementType type;
	private String statement;
	private boolean returnGeneratedKeys;

	public OlympaStatement(StatementTypeInsertDelete type, String tableName, String... keys) {
		this.type = type;
		StringJoiner sj = new StringJoiner(" ");
		sj.add(type.get());
		sj.add(tableName);
		if (type.isSame(StatementType.INSERT)) {
			StringJoiner sj2 = new StringJoiner(", ", "(", ")");
			for (String key : keys)
				sj2.add("`" + key + "`");
			sj.add(sj2.toString());
			sj.add("VALUES");
			sj2 = new StringJoiner(", ", "(", ")");
			int i = -1;
			while (++i < keys.length)
				sj2.add("?");
			sj.add(sj2.toString());
		} else {
			sj.add("WHERE");
			StringJoiner sj2 = new StringJoiner(" AND ");
			Arrays.stream(keys).forEach(w -> sj2.add("`" + w + "` = ?"));
			sj.add(sj2.toString());
		}
		statement = sj.toString() + ";";
		returnGeneratedKeys = true;
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String what, String[] keys) {
		this(type, tableName, new String[] { what }, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, String... keys) {
		this(type, tableName, what, 0, 0, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, int offset, int limit, String... keys) {
		this(type, tableName, what, null, null, limit, offset, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, String orderCollumn, Boolean asc, String... keys) {
		this(type, tableName, what, orderCollumn, asc, 0, 0, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, String orderCollumn, Boolean asc, int offset, int limit, String... keys) {
		this(type, tableName, what, null, orderCollumn, asc, offset, limit, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, String[] or, String orderCollumn, Boolean asc, int offset, int limit, String... keys) {
		this.type = type;
		StringJoiner sj = new StringJoiner(" ");
		sj.add(type.get());
		if (type.isSame(StatementType.SELECT)) {
			if (keys == null || keys.length == 0)
				keys = new String[] { "*" };
			sj.add(String.join(", ", keys));
			sj.add("FROM");
		}
		sj.add(tableName);
		if (type.isSame(StatementType.UPDATE)) {
			sj.add("SET");
			StringJoiner sj2 = new StringJoiner(", ");
			Arrays.stream(keys).forEach(key -> sj2.add("`" + key + "` = ?"));
			sj.add(sj2.toString());
		}
		if (what != null && what.length != 0) {
			sj.add("WHERE");
			StringJoiner sj2 = new StringJoiner(" AND ");
			Arrays.stream(what).forEach(w -> sj2.add("`" + w + "` = ?"));
			sj.add(sj2.toString());
		}
		if (or != null && or.length != 0) {
			sj.add("OR");
			StringJoiner sj2 = new StringJoiner(" OR ");
			Arrays.stream(or).forEach(w -> sj2.add("`" + w + "` = ?"));
			sj.add(sj2.toString());
		}
		if (orderCollumn != null && !orderCollumn.isBlank() && asc != null)
			sj.add("ORDER BY " + orderCollumn + " " + (asc ? "ASC" : "DESC"));
		if (limit > 0)
			sj.add("LIMIT " + limit);
		if (offset > 0)
			sj.add("OFFSET " + offset);
		statement = sj.toString() + ";";
		returnGeneratedKeys = true;
	}

	public OlympaStatement(StatementTypeDefault type, String tableName) {
		this.type = type;
		StringJoiner sj = new StringJoiner(" ");
		sj.add(type.get());
		sj.add(tableName);
		statement = sj.toString() + ";";
		returnGeneratedKeys = true;
	}

	public OlympaStatement(String statement) {
		this(statement, false);
	}

	public OlympaStatement(String statement, boolean returnGeneratedKeys) {
		this.statement = statement;
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	private PreparedStatement prepared;

	public PreparedStatement getStatement() throws SQLException {
		if (prepared == null || prepared.isClosed() || !prepared.getConnection().isValid(0))
			prepared = returnGeneratedKeys ? LinkSpigotBungee.Provider.link.getDatabase().prepareStatement(statement, Statement.RETURN_GENERATED_KEYS) : LinkSpigotBungee.Provider.link.getDatabase().prepareStatement(statement);
		return prepared;
	}

	public String getStatementCommand() {
		return statement;
	}

	public int execute() throws SQLException {
		try {
			return getStatement().executeUpdate();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new SQLException("OlympaStatement " + (type != null ? "Type " + type.getTypeName() : "") + ": " + statement);
		}
	}

	public ResultSet executeQuery() throws SQLException {
		try {
			return getStatement().executeQuery();
		} catch (SQLException e) {
			throw new SQLException("OlympaStatement " + (type != null ? "Type " + type.getTypeName() : "") + ": " + statement, e.getCause());
		}
	}
}
