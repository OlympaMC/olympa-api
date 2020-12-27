package fr.olympa.api.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.sql.SQLColumn.SQLUpdater;
import fr.olympa.api.sql.statement.OlympaStatement;

public class SQLTable<T> {
	
	private final String name;
	private final List<SQLColumn<T>> columns;
	private final SQLColumn<T> primaryColumn;
	
	private OlympaStatement insertStatement, deleteStatement;
	
	public SQLTable(String name, List<SQLColumn<T>> columns) {
		this.name = name;
		this.columns = columns;
		this.primaryColumn = columns.stream().filter(SQLColumn::isPrimaryKey).findAny().orElseThrow(() -> new IllegalArgumentException("Can't create a table without primary key."));
	}
	
	public String getName() {
		return name;
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
		ResultSet columnsSet = link.getDatabase().getMetaData().getColumns(null, schemaPattern, tablePattern, "%");
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
		}else { // la table n'existe pas : il faut la créer
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + name + " (" + SQLColumn.toParameters(columns) + ")");
			link.sendMessage("Table SQL §6%s §ecréée !", name);
		}
		
		columns.stream().filter(SQLColumn::isUpdatable).forEach(column -> column.setSQLUpdater(new SQLUpdater<>() {
			private OlympaStatement updateStatement = new OlympaStatement("UPDATE " + name + " SET " + column.getName() + " = ? WHERE (" + primaryColumn.getName() + " = ?)");
			
			@Override
			public synchronized void update(T object, Object sqlObject, int sqlType) throws SQLException {
				PreparedStatement statement = updateStatement.getStatement();
				statement.setObject(1, sqlObject, sqlType);
				statement.setObject(2, primaryColumn.getPrimaryKeySQLObject(object), primaryColumn.getSQLType());
				statement.executeUpdate();
				statement.close();
			}
			
			@Override
			public void updateAsync(T object, Object sqlObject, int sqlType, Runnable successCallback, Consumer<SQLException> failCallback) {
				link.launchAsync(() -> {
					try {
						update(object, sqlObject, sqlType);
						if (successCallback != null) successCallback.run();
					}catch (SQLException e) {
						if (failCallback == null) {
							e.printStackTrace();
						}else failCallback.accept(e);
					}
				});
			}
		}));
		
		StringJoiner valuesJoiner = new StringJoiner(", ", "(", ")");
		for (int i = 0; i < columns.stream().filter(SQLColumn::isNotDefault).count(); i++) valuesJoiner.add("?");
		
		insertStatement = new OlympaStatement(
				"INSERT INTO " + name + " (" + columns.stream().filter(SQLColumn::isNotDefault).map(SQLColumn::getName).collect(Collectors.joining(", ")) + ")"
						+ " VALUES " + valuesJoiner.toString(), true);
		
		deleteStatement = new OlympaStatement("DELETE FROM " + name + " WHERE (" + primaryColumn.getName() + " = ?)");
		
		return this;
	}
	
	public synchronized ResultSet insert(Object... notDefaultObjects) throws SQLException {
		PreparedStatement statement = insertStatement.getStatement();
		int i = 1;
		for (SQLColumn<T> column : columns) {
			if (column.isNotDefault()) {
				statement.setObject(i, notDefaultObjects[i - 1], column.getSQLType());
				i++;
			}
		}
		//LinkSpigotBungee.Provider.link.sendMessage("Création d'une ligne sur la table %s (données: %s).", name, Arrays.toString(notDefaultObjects));
		statement.executeUpdate();
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
