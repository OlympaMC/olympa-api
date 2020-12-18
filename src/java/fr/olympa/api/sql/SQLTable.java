package fr.olympa.api.sql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import fr.olympa.api.sql.SQLColumn.SQLUpdater;
import fr.olympa.api.sql.statement.OlympaStatement;
import fr.olympa.core.spigot.OlympaCore;

public class SQLTable<T> {
	
	private final String name, quotedName;
	private final List<SQLColumn<T>> columns;
	private final SQLColumn<T> primaryColumn;
	
	private OlympaStatement insertStatement, deleteStatement, selectStatement;
	
	public SQLTable(String name, List<SQLColumn<T>> columns) {
		this.name = name;
		this.quotedName = "`" + name + "`";
		this.columns = columns;
		this.primaryColumn = columns.stream().filter(SQLColumn::isPrimaryKey).findAny().orElseThrow(() -> new IllegalArgumentException("Can't create a table without primary key."));
	}
	
	public String getName() {
		return name;
	}
	
	public String getQuotedName() {
		return quotedName;
	}
	
	public List<SQLColumn<T>> getColumns() {
		return columns;
	}
	
	public SQLTable<T> createOrAlter() throws SQLException {
		Statement statement = OlympaCore.getInstance().getDatabase().createStatement();
		
		ResultSet columnsSet = OlympaCore.getInstance().getDatabase().getMetaData().getColumns(null, null, name, "%");
		if (columnsSet.first()) { // la table existe : il faut vérifier si toutes les colonnes sont présentes
			List<SQLColumn<?>> missingColumns = new ArrayList<>(columns);
			while (columnsSet.next()) {
				String columnName = columnsSet.getString(4);
				if (!missingColumns.removeIf(column -> column.getName().equals(columnName)))
					OlympaCore.getInstance().sendMessage("§cColonne %s présente dans la table SQL %s mais non déclarée.", columnName, name);
			}
			for (SQLColumn<?> column : missingColumns) {
				statement.executeUpdate("ALTER TABLE " + quotedName + " ADD " + column.toDeclaration());
				OlympaCore.getInstance().sendMessage("La colonne §6%s §ea été créée dans la table §6%s§e.", column.toDeclaration(), name);
			}
		}else { // la table n'existe pas : il faut la créer
			statement.executeUpdate("CREATE TABLE IF NOT EXISTS " + quotedName + " (" + SQLColumn.toParameters(columns) + ")");
			OlympaCore.getInstance().sendMessage("Table SQL §6%s §ecréée !", name);
		}
		
		columns.stream().filter(SQLColumn::isUpdatable).forEach(column -> column.setSQLUpdater(new SQLUpdater<>() {
			private OlympaStatement updateStatement = new OlympaStatement("UPDATE " + quotedName + " SET " + column.getName() + " = ? WHERE (" + primaryColumn.getName() + " = ?)");
			
			@Override
			public synchronized void update(T object, Object sqlObject, int sqlType) throws SQLException {
				PreparedStatement statement = updateStatement.getStatement();
				statement.setObject(1, sqlObject, sqlType);
				statement.setObject(2, primaryColumn.getPrimaryKeySQLObject(object), primaryColumn.getSQLType());
				statement.executeUpdate();
				statement.close();
			}
		}));
		
		insertStatement = new OlympaStatement(
				"INSERT INTO " + quotedName + " (" + columns.stream().filter(SQLColumn::isNotDefault).map(SQLColumn::getName).collect(Collectors.joining(", ")) + ")"
						+ " VALUES (" + "?, ".repeat((int) columns.stream().filter(SQLColumn::isNotDefault).count()) + ")", true);
		
		deleteStatement = new OlympaStatement("DELETE FROM " + quotedName + " WHERE (" + primaryColumn.getName() + " = ?)");
		
		selectStatement = new OlympaStatement("SELECT * FROM " + quotedName + " WHERE (" + primaryColumn.getName() + " = ?)");
		
		return this;
	}
	
	public synchronized ResultSet insert(Object... notDefaultObjects) throws SQLException {
		PreparedStatement statement = insertStatement.getStatement();
		int i = 1;
		for (SQLColumn<T> column : columns) {
			if (column.isNotDefault()) {
				statement.setObject(i++, notDefaultObjects[i - 2], column.getSQLType());
			}
		}
		statement.executeUpdate();
		return statement.getGeneratedKeys();
	}
	
	public synchronized void delete(T object) throws SQLException {
		PreparedStatement statement = deleteStatement.getStatement();
		statement.setObject(1, primaryColumn.getPrimaryKeySQLObject(object), primaryColumn.getSQLType());
		statement.executeUpdate();
	}
	
	public synchronized ResultSet get(Object primaryObject) throws SQLException {
		PreparedStatement statement = selectStatement.getStatement();
		statement.setObject(1, primaryObject, primaryColumn.getSQLType());
		return statement.executeQuery();
	}
	
}
