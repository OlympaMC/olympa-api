package fr.olympa.api.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.sql.SQLColumn.SQLSelector;
import fr.olympa.api.sql.SQLColumn.SQLUpdater;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.api.sql.statement.StatementType;
import fr.olympa.api.utils.CacheStats;

public class SQLTable<T> {

	private final String name;
	private final List<SQLColumn<T>> columns;
	private final SQLColumn<T> primaryColumn;
	private Function<ResultSet, T> initializeFromRow;

	private OlympaStatement insertStatement, deleteStatement;

	public SQLTable(String name, List<SQLColumn<T>> columns) {
		this.name = name;
		this.columns = columns;
		this.primaryColumn = columns.stream().filter(SQLColumn::isPrimaryKey).findAny().orElseThrow(() -> new IllegalArgumentException("Can't create a table without primary key."));
		CacheStats.addDebugList("SQL_COLUMNS_OF_TABLE_" + name, columns);
	}

	public SQLTable(String name, List<SQLColumn<T>> columns, Function<ResultSet, T> initializeFromRow) {
		this(name, columns);
		this.initializeFromRow = initializeFromRow;
	}

	public String getName() {
		return name;
	}

	public synchronized void update(T object, Map<SQLColumn<?>, Object> sqlObjects) throws SQLException {
		OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, name, primaryColumn.getName(), sqlObjects.entrySet().stream().map(e -> ((SQLColumn<?>) e.getValue()).getName()).toArray(String[]::new));
		PreparedStatement statement = updateStatement.getStatement();
		int i = 1;
		for (Entry<SQLColumn<?>, Object> entry : sqlObjects.entrySet())
			statement.setObject(i++, entry.getValue(), entry.getKey().getSQLType());
		statement.setObject(i, primaryColumn.getPrimaryKeySQLObject(object), primaryColumn.getSQLType());
		statement.executeUpdate();
		statement.close();
	}

	public void updateAsync(T object, Map<SQLColumn<?>, Object> sqlObjects, Runnable successCallback, Consumer<SQLException> failCallback) {
		LinkSpigotBungee.Provider.link.launchAsync(() -> {
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
		LinkSpigotBungee link = LinkSpigotBungee.Provider.link;
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
		} else if (statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + name + " (" + SQLColumn.toParameters(columns) + ")") >= 1)
			link.sendMessage("Table SQL §6%s §ecréée !", name);
		else
			new IllegalAccessError(
					"La table " + name + " (table pattern: " + tablePattern + ", schema pattern: " + schemaPattern + ") n'a pas été trouvée dans le catalog alors qu'elle existe bien. Impossible de vérifier son intégralitée de colonne.")
							.printStackTrace();

		columns.forEach(column -> column.setSQLSelector(new SQLSelector<>() {

			@Override
			public List<T> select(Object sqlObject, String... specifiedColumnsReturned) throws SQLException, IllegalAccessException {
				List<T> objects = new ArrayList<>();
				if (initializeFromRow == null)
					throw new IllegalAccessException("Function initializeFromRow is not set for table " + name + ", unable to automate select data.");
				ResultSet rs = selectBasic(sqlObject, specifiedColumnsReturned);
				while (rs.next())
					objects.add(initializeFromRow.apply(rs));
				rs.close();
				return objects;
			}

			@Override
			public ResultSet selectBasic(Object sqlObject, String... specifiedColumnsReturned) throws SQLException {
				PreparedStatement statement = new OlympaStatement(StatementType.SELECT, name, column.getName(), specifiedColumnsReturned).getStatement();
				int i = 1;
				if (specifiedColumnsReturned != null && specifiedColumnsReturned.length != 0)
					statement.setString(i++, String.join(",", specifiedColumnsReturned));
				statement.setObject(i, sqlObject, column.getSQLType());
				return statement.executeQuery();
			}
		}));
		columns.stream().filter(SQLColumn::isUpdatable).forEach(column -> column.setSQLUpdater(new SQLUpdater<>() {
			//			private OlympaStatement updateStatement = new OlympaStatement("UPDATE " + name + " SET " + column.getName() + " = ? WHERE (" + primaryColumn.getName() + " = ?)");
			private OlympaStatement updateStatement = new OlympaStatement(StatementType.UPDATE, name, primaryColumn.getName(), new String[] { column.getName() });

			@Override
			public void update(T object, Object sqlObject, int sqlType) throws SQLException {
				PreparedStatement statement = updateStatement.getStatement();
				statement.setObject(1, sqlObject, sqlType);
				statement.setObject(2, primaryColumn.getPrimaryKeySQLObject(object), primaryColumn.getSQLType());
				updateStatement.executeUpdate();
				statement.close();
			}

			@Override
			public void updateAsync(T object, Object sqlObject, int sqlType, Runnable successCallback, Consumer<SQLException> failCallback) {
				link.launchAsync(() -> {
					try {
						update(object, sqlObject, sqlType);
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
		PreparedStatement statement = insertStatement.getStatement();
		int i = 1;
		for (SQLColumn<T> column : columns)
			if (column.isNotDefault()) {
				statement.setObject(i, notDefaultObjects[i - 1], column.getSQLType());
				i++;
			}
		//LinkSpigotBungee.Provider.link.sendMessage("Création d'une ligne sur la table %s (données: %s).", name, Arrays.toString(notDefaultObjects));tqt
		insertStatement.executeUpdate();
		return statement.getGeneratedKeys();
	}

	public synchronized void delete(T primaryObject) throws SQLException {
		PreparedStatement statement = deleteStatement.getStatement();
		statement.setObject(1, primaryColumn.getPrimaryKeySQLObject(primaryObject), primaryColumn.getSQLType());
		statement.executeUpdate();
	}

	public synchronized void deleteSQLObject(Object primaryObjectSQL) throws SQLException {
		PreparedStatement statement = deleteStatement.getStatement();
		statement.setObject(1, primaryObjectSQL, primaryColumn.getSQLType());
		statement.executeUpdate();
	}

	public synchronized ResultSet get(Object primaryObject) throws SQLException {
		return getFromColumn(primaryColumn, primaryObject);
	}

	public synchronized ResultSet getFromColumn(SQLColumn<T> column, Object object) throws SQLException {
		PreparedStatement statement = column.getSelectStatement(this).getStatement();
		statement.setObject(1, object, column.getSQLType());
		return statement.executeQuery();
	}

}
