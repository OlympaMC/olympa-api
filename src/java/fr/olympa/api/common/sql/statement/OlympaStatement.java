package fr.olympa.api.common.sql.statement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.StringJoiner;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.utils.Utils;

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
	private String statementCommand;
	private boolean returnGeneratedKeys = false;

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
		statementCommand = sj.toString() + ";";
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String what, String[] keys) {
		this(type, tableName, new String[] { what }, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, int limit, String... keys) {
		this(type, tableName, what, null, null, null, 0, limit, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, String... keys) {
		this(type, tableName, what, 0, 0, keys);
	}

	public OlympaStatement(StatementTypeSelectUpdate type, String tableName, String[] what, int offset, int limit, String... keys) {
		this(type, tableName, what, null, null, offset, limit, keys);
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
			Arrays.stream(keys).forEach(key -> sj2.add(acuteIfNeeded(key) + " = ?"));
			sj.add(sj2.toString());
		}
		if (what != null && what.length != 0) {
			sj.add("WHERE");
			StringJoiner sj2 = new StringJoiner(" AND ");
			Arrays.stream(what).forEach(w -> sj2.add(acuteIfNeeded(w) + " = ?"));
			sj.add(sj2.toString());
		}
		if (or != null && or.length != 0) {
			sj.add("OR");
			StringJoiner sj2 = new StringJoiner(" OR ");
			Arrays.stream(or).forEach(w -> sj2.add(acuteIfNeeded(w) + " = ?"));
			sj.add(sj2.toString());
		}
		if (orderCollumn != null && !orderCollumn.isBlank() && asc != null)
			sj.add("ORDER BY " + orderCollumn + " " + (asc ? "ASC" : "DESC"));
		if (limit > 0)
			sj.add("LIMIT " + limit);
		if (offset > 0)
			sj.add("OFFSET " + offset);
		statementCommand = sj.toString() + ";";
	}

	public OlympaStatement(StatementTypeDefault type, String tableName) {
		this.type = type;
		StringJoiner sj = new StringJoiner(" ");
		sj.add(type.get());
		sj.add(tableName);
		statementCommand = sj.toString() + ";";
	}

	public OlympaStatement(String statement) {
		this(statement, false);
	}

	public OlympaStatement(String statement, boolean returnGeneratedKeys) {
		statementCommand = statement;
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	public OlympaStatement(String statement, String tableName, boolean returnGeneratedKeys) {
		statementCommand = String.format(statement, tableName);
		this.returnGeneratedKeys = returnGeneratedKeys;
	}

	/*private PreparedStatement prepared;
	private ReentrantLock lock = new ReentrantLock();*/

	public PreparedStatement createStatement() throws SQLException {
		/*if (!lock.tryLock(10, TimeUnit.SECONDS)) throw new SQLException("Le thread n'a pas été acquis au bout de 10 secondes");
		if (prepared == null || prepared.isClosed() || !prepared.getConnection().isValid(0))
			prepared = returnGeneratedKeys ? LinkSpigotBungee.getInstance().getDatabase().prepareStatement(statementCommand, Statement.RETURN_GENERATED_KEYS) : LinkSpigotBungee.getInstance().getDatabase().prepareStatement(statementCommand);
		return prepared;*/
		return returnGeneratedKeys ? LinkSpigotBungee.getInstance().getDatabase().prepareStatement(statementCommand, Statement.RETURN_GENERATED_KEYS) : LinkSpigotBungee.getInstance().getDatabase().prepareStatement(statementCommand);
	}

	private String acuteIfNeeded(String s) {
		if (s.charAt(0) != '`' && s.charAt(s.length() - 1) != '`')
			return "`" + s + "`";
		return s;
	}

	public String getStatementCommand() {
		return statementCommand;
	}

	public OlympaStatement returnGeneratedKeys() {
		returnGeneratedKeys = true;
		return this;
	}

	private void sendError(Exception e) throws SQLException {
		throw new SQLException("OlympaStatement " + (type != null ? "Type " + type.getTypeName() : "") + ": " + statementCommand, e);
	}

	public int executeUpdate(PreparedStatement statement) throws SQLException {
		try {
			return statement.executeUpdate();
		} catch (Exception e) {
			sendError(e);
			return -42;
		}
	}

	public static long time = 0;

	public ResultSet executeQuery(PreparedStatement statement) throws SQLException {
		try {
			return statement.executeQuery();
		} catch (SQLNonTransientConnectionException e) {
			if (time == 0) {
				time = Utils.getCurrentTimeInSeconds();
				return executeQuery(statement);
			}
			sendError(e);
			return null;
		} catch (Exception e) {
			sendError(e);
			return null;
		}
	}

	/*public int executeUpdate(StatementProvider statementProvider) throws SQLException {
		try {
			PreparedStatement statement = getStatement();
			statementProvider.call(statement);
			return statement.executeUpdate();
		}catch (Exception e) {
			throw new SQLException("OlympaStatement " + (type != null ? "Type " + type.getTypeName() : "") + ": " + statementCommand, e);
		}finally {
			close();
		}
	}

	public int executeUpdate(StatementProvider statementProvider, ResultSetProvider generatedKeysProvider) throws SQLException {
		try {
			PreparedStatement statement = getStatement();
			statementProvider.call(statement);
			int update = statement.executeUpdate();
			ResultSet resultSet = statement.getGeneratedKeys();
			generatedKeysProvider.call(resultSet);
			resultSet.close();
			return update;
		}catch (Exception e) {
			throw new SQLException("OlympaStatement " + (type != null ? "Type " + type.getTypeName() : "") + ": " + statementCommand, e);
		}finally {
			close();
		}
	}

	public void executeQuery(StatementProvider statementProvider, ResultSetProvider queryProvider) throws SQLException {
		try {
			PreparedStatement statement = getStatement();
			statementProvider.call(statement);
			ResultSet resultSet = statement.executeQuery();
			queryProvider.call(resultSet);
			resultSet.close();
		}catch (Exception e) {
			throw new SQLException("OlympaStatement " + (type != null ? "Type " + type.getTypeName() : "") + ": " + statementCommand, e);
		}finally {
			close();
		}
	}

	@FunctionalInterface
	public interface StatementProvider {
		public void call(PreparedStatement statement) throws Exception;
	}

	@FunctionalInterface
	public interface ResultSetProvider {
		public void call(ResultSet resultSet) throws Exception;
	}*/

}
