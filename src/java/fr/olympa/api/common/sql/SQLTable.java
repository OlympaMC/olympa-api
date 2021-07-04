package fr.olympa.api.common.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.sql.SQLColumn.SQLSelector;
import fr.olympa.api.common.sql.SQLColumn.SQLUpdater;
import fr.olympa.api.common.sql.statement.OlympaStatement;
import fr.olympa.api.common.sql.statement.StatementType;
import fr.olympa.api.utils.CacheStats;

public class SQLTable<T> {

	private final String name;
	private final List<SQLColumn<T>> columns;
	private final SQLColumn<T> primaryColumn;
	public ObjectInitizalizer<T> initializeFromRow;

	private OlympaStatement insertStatement, deleteStatement;

	public SQLTable(String name, List<SQLColumn<T>> columns) {
		this.name = name; // TODO add ` like `db`.`table`
		this.columns = columns;
		this.primaryColumn = columns.stream().filter(SQLColumn::isPrimaryKey).findAny().orElseThrow(() -> new IllegalArgumentException("Can't create a table without primary key."));
		CacheStats.addDebugList("SQL_COLUMNS_OF_TABLE_" + name, columns);
	}

	public SQLTable(String name, List<SQLColumn<T>> columns, ObjectInitizalizer<T> initializeFromRow) {
		this(name, columns);
		this.initializeFromRow = initializeFromRow;
	}

	public String getName() {
		return name;
	}

	public String getCleanName() {
		return name.replace("`", "");
	}

	public void update(T object, Map<SQLColumn<?>, Object> sqlObjects) throws SQLException {
		OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, name, primaryColumn.getName(), sqlObjects.keySet().stream().map(e -> e.getName()).toArray(String[]::new));
		try (PreparedStatement statement = updateStatement.createStatement()) {
			int i = 1;
			for (Entry<SQLColumn<?>, Object> entry : sqlObjects.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof SQLNullObject)
					value = null;
				statement.setObject(i++, value, entry.getKey().getSQLType());
			}
			statement.setObject(i, primaryColumn.getPrimaryKeySQLObject(object), primaryColumn.getSQLType());
			updateStatement.executeUpdate(statement);
		}
	}

	public synchronized List<T> select(Map<SQLColumn<?>, Object> whereSqlObjects) throws SQLException, IllegalAccessException {
		if (initializeFromRow == null)
			throw new IllegalAccessException("Function initializeFromRow is not set for table " + name + ", unable to automate select data.");
		OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, name, whereSqlObjects.keySet().stream().map(e -> e.getName()).toArray(String[]::new));
		try (PreparedStatement statement = selectStatement.createStatement()) {
			int i = 1;
			for (Entry<SQLColumn<?>, Object> entry : whereSqlObjects.entrySet()) {
				Object value = entry.getValue();
				if (value instanceof SQLNullObject)
					value = null;
				statement.setObject(i++, value, entry.getKey().getSQLType());
			}
			ResultSet rs = selectStatement.executeQuery(statement);
			List<T> objects = new ArrayList<>();
			while (rs.next()) objects.add(initializeFromRow.initialize(rs));
			rs.close();
			return objects;
		}
	}

	public void updateAsync(T object, Map<SQLColumn<?>, Object> sqlObjects, Runnable successCallback, Consumer<SQLException> failCallback) {
		LinkSpigotBungee.getInstance().launchAsync(() -> {
			try {
				update(object, sqlObjects);
				if (successCallback != null)
					successCallback.run();
			} catch (SQLException e) {
				if (failCallback == null)
					e.printStackTrace();
				else
					failCallback.accept(e);
			}
		});
	}

	public SQLTable<T> createOrAlter() throws SQLException {
		LinkSpigotBungee link = LinkSpigotBungee.getInstance();
		Statement statement = link.getDatabase().createStatement();

		String schemaPattern = null, tablePattern = name;
		int pointIndex = name.indexOf('.');
		if (pointIndex >= 0) {
			schemaPattern = name.substring(0, pointIndex);
			tablePattern = name.substring(pointIndex + 1);
		}
		ResultSet columnsSet = link.getDatabase().getMetaData().getColumns(schemaPattern, null, tablePattern, "%");
		if (columnsSet.first()) { // la table existe : il faut vérifier si toutes les colonnes sont présentes
			link.sendMessage("§7Chargement de la table %s...", name);
			List<SQLColumn<?>> missingColumns = columns.stream().filter(Predicate.not(SQLColumn::isPrimaryKey)).collect(Collectors.toList());
			while (columnsSet.next()) {
				String columnName = "`" + columnsSet.getString(4) + "`";
				if (!missingColumns.removeIf(column -> column.getName().equals(columnName)))
					link.sendMessage("§cColonne %s présente dans la table SQL %s mais non déclarée.", columnName, name);
			}
			for (SQLColumn<?> column : missingColumns) {
				statement.executeUpdate("ALTER TABLE " + name + " ADD " + column.toDeclaration());
				link.sendMessage("La colonne §6%s §ea été créée dans la table §6%s§e.", column.toDeclaration(), name);
			}
		}else {
			int updateResult = statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + name + " (" + SQLColumn.toParameters(columns) + ")");
			columnsSet.close();
			columnsSet = link.getDatabase().getMetaData().getColumns(schemaPattern, null, tablePattern, "%");
			if (updateResult < 1 && !columnsSet.first()) {
				new IllegalAccessError(
						"La table " + name + " (table pattern: " + tablePattern + ", schema pattern: " + schemaPattern + ") n'a pas été trouvée dans le catalog alors qu'elle existe bien. Impossible de vérifier son intégralitée de colonne.")
				.printStackTrace();
			}else {
				link.sendMessage("Table SQL §6%s §ecréée !", name);
			}
		}
		statement.close();

		columns.forEach(column -> column.setSQLSelector(new SQLSelector<>() {

			@Override
			public List<T> select(Object sqlObject, String... specifiedColumnsReturned) throws SQLException, IllegalAccessException {
				List<T> objects = new ArrayList<>();
				if (initializeFromRow == null)
					throw new IllegalAccessException("Function initializeFromRow is not set for table " + name + ", unable to automate select data.");
				ResultSet rs = selectBasic(sqlObject, specifiedColumnsReturned);
				while (rs.next())
					objects.add(initializeFromRow.initialize(rs));
				rs.close();
				return objects;
			}

			@Override
			public ResultSet selectBasic(Object sqlObject, String... specifiedColumnsReturned) throws SQLException {
				OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, name, column.getName(), specifiedColumnsReturned);
				try (PreparedStatement statement = selectStatement.createStatement()) {
					statement.setObject(1, sqlObject, column.getSQLType());
					return selectStatement.executeQuery(statement);
				}
			}
		}));
		columns.stream().filter(SQLColumn::isUpdatable).forEach(column -> column.setSQLUpdater(new SQLUpdater<>() {
			//			private OlympaStatement updateStatement = new OlympaStatement("UPDATE " + name + " SET " + column.getName() + " = ? WHERE (" + primaryColumn.getName() + " = ?)");
			private OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, name, primaryColumn.getName(), new String[] { column.getName() });

			@Override
			public synchronized void update(T object, Object sqlObject, int sqlType) throws SQLException {
				try (PreparedStatement statement = updateStatement.createStatement()) {
					statement.setObject(1, sqlObject, sqlType);
					statement.setObject(2, primaryColumn.getPrimaryKeySQLObject(object), primaryColumn.getSQLType());
					updateStatement.executeUpdate(statement);
				}
			}

			@Override
			public void updateAsync(T object, Object sqlObject, int sqlType, Runnable successCallback, Consumer<SQLException> failCallback) {
				link.launchAsync(() -> {
					try {
						update(object, sqlObject, sqlType);
						if (successCallback != null)
							successCallback.run();
					} catch (SQLException e) {
						e.printStackTrace();
						if (failCallback != null)
							failCallback.accept(e);
					}
				});
			}
		}));

		StringJoiner valuesJoiner = new StringJoiner(", ", "(", ")");
		for (int i = 0; i < columns.stream().filter(SQLColumn::isNotDefault).count(); i++)
			valuesJoiner.add("?");

		insertStatement = new OlympaStatement("INSERT INTO " + name + " (" + columns.stream().filter(SQLColumn::isNotDefault).map(SQLColumn::getName).collect(Collectors.joining(", ")) + ")"
				+ " VALUES " + valuesJoiner.toString(),
				true);

		deleteStatement = new OlympaStatement("DELETE FROM " + name + " WHERE (" + primaryColumn.getName() + " = ?)");

		return this;
	}

	public synchronized ResultSet insert(Object... notDefaultObjects) throws SQLException {
		try (PreparedStatement statement = insertStatement.createStatement()) {
			int i = 1;
			for (SQLColumn<T> column : columns) if (column.isNotDefault()) {
				statement.setObject(i, notDefaultObjects[i - 1], column.getSQLType());
				i++;
			}
			//LinkSpigotBungee.getInstance().sendMessage("Création d'une ligne sur la table %s (données: %s).", name, Arrays.toString(notDefaultObjects));tqt
			insertStatement.executeUpdate(statement);
			return statement.getGeneratedKeys();
		}
	}

	public void insertAsync(Consumer<ResultSet> successCallback, Consumer<SQLException> failCallback, Object... notDefaultObjects) {
		LinkSpigotBungee.getInstance().launchAsync(() -> {
			try {
				ResultSet resultSet = insert(notDefaultObjects);
				if (successCallback != null) successCallback.accept(resultSet);
				resultSet.close();
			}catch (SQLException e) {
				e.printStackTrace();
				if (failCallback != null) failCallback.accept(e);
			}
		});
	}
	
	public synchronized void delete(T primaryObject) throws SQLException {
		try (PreparedStatement statement = deleteStatement.createStatement()) {
			statement.setObject(1, primaryColumn.getPrimaryKeySQLObject(primaryObject), primaryColumn.getSQLType());
			deleteStatement.executeUpdate(statement);
		}
	}
	
	public void deleteAsync(T primaryObject, Runnable successCallback, Consumer<SQLException> failCallback) {
		LinkSpigotBungee.getInstance().launchAsync(() -> {
			try {
				delete(primaryObject);
				if (successCallback != null)
					successCallback.run();
			}catch (SQLException e) {
				e.printStackTrace();
				if (failCallback != null)
					failCallback.accept(e);
			}
		});
	}
	
	public void deleteMulti(T... primaryObjects) throws SQLException {
		if (primaryObjects.length == 1) {
			delete(primaryObjects[0]);
			return;
		}
		
		OlympaStatement deletionStatement = new OlympaStatement("DELETE FROM " + name + " WHERE " + primaryColumn.getName() + " IN (" + Arrays.stream(primaryObjects).map(x -> primaryColumn.getPrimaryKeySQLObject(x).toString()).collect(Collectors.joining(", ")) + ")");
		try (PreparedStatement statement = deletionStatement.createStatement()) {
			deletionStatement.executeUpdate(statement);
		}
	}
	
	public void deleteMultiAsync(Runnable successCallback, Consumer<SQLException> failCallback, T... primaryObjects) {
		LinkSpigotBungee.getInstance().launchAsync(() -> {
			try {
				deleteMulti(primaryObjects);
				if (successCallback != null)
					successCallback.run();
			}catch (SQLException e) {
				e.printStackTrace();
				if (failCallback != null)
					failCallback.accept(e);
			}
		});
	}

	public synchronized void deleteSQLObject(Object primaryObjectSQL) throws SQLException {
		try (PreparedStatement statement = deleteStatement.createStatement()) {
			statement.setObject(1, primaryObjectSQL, primaryColumn.getSQLType());
			deleteStatement.executeUpdate(statement);
		}
	}
	
	public void deleteSQLObjectAsync(Object primaryObjectSQL, Runnable successCallback, Consumer<SQLException> failCallback) {
		LinkSpigotBungee.getInstance().launchAsync(() -> {
			try {
				deleteSQLObject(primaryObjectSQL);
				if (successCallback != null)
					successCallback.run();
			}catch (SQLException e) {
				e.printStackTrace();
				if (failCallback != null)
					failCallback.accept(e);
			}
		});
	}

	public synchronized ResultSet get(Object primaryObject) throws SQLException {
		return getFromColumn(primaryColumn, primaryObject);
	}

	public synchronized ResultSet getFromColumn(SQLColumn<T> column, Object object) throws SQLException {
		OlympaStatement selectStatement = column.getSelectStatement(this);
		try (PreparedStatement statement = selectStatement.createStatement()) {
			statement.setObject(1, object, column.getSQLType());
			return selectStatement.executeQuery(statement);
		}
	}

	public synchronized List<T> selectAll(ObjectInitializationHandler exceptionHandler) throws SQLException {
		if (initializeFromRow == null)
			throw new IllegalStateException("Function initializeFromRow is not set for table " + name + ", unable to automate select data.");
		List<T> objects = new ArrayList<>();
		OlympaStatement selectStatement = new OlympaStatement(StatementType.SELECT, name, null);
		try (PreparedStatement statement = selectStatement.createStatement()) {
			ResultSet rs = selectStatement.executeQuery(statement);
			while (rs.next()) {
				try {
					objects.add(initializeFromRow.initialize(rs));
				}catch (Exception ex) {
					if (exceptionHandler != null && exceptionHandler.fail(ex, rs)) continue;
					throw ex;
				}
			}
			rs.close();
			return objects;
		}
	}

	public interface ObjectInitizalizer<T> {
		public T initialize(ResultSet resultSet) throws SQLException;
	}
	
	public interface ObjectInitializationHandler {
		public boolean fail(Exception ex, ResultSet resultSet) throws SQLException;
	}
	
}
