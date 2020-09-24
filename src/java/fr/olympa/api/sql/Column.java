package fr.olympa.api.sql;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

import fr.olympa.api.sql.statement.OlympaStatement;

public class Column<T> {
	
	private final String name;
	private final String type;
	private boolean primaryKey = false;
	private boolean updatable = false;
	
	private SQLUpdater<T> sqlUpdater;
	
	public Column(String name, String type) {
		this.name = "`" + name + "`";
		this.type = type;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public Column<T> setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
		return this;
	}
	
	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public boolean isUpdatable() {
		return updatable;
	}
	
	public Column<T> setUpdatable(boolean updatable) {
		this.updatable = updatable;
		return this;
	}
	
	public void updateValue(T object, Object sqlObject, int sqlType) throws SQLException {
		if (sqlUpdater != null) sqlUpdater.update(object, sqlObject, sqlType);
	}
	
	public void setSQLUpdater(SQLUpdater<T> statementCreation) {
		Validate.isTrue(updatable, "Cannot assign SQL Updater to non-updatable column.");
		this.sqlUpdater = statementCreation;
	}
	
	@Override
	public String toString() {
		return name + " " + type;
	}
	
	@FunctionalInterface
	public interface SQLUpdater<T> {
		void update(T object, Object sqlObject, int sqlType) throws SQLException;
	}
	
	public static <T> String toParameters(List<Column<T>> columns) {
		String parameters = columns.stream().map(Column::toString).collect(Collectors.joining(", "));
		Optional<Column<T>> primaryKey = columns.stream().filter(Column::isPrimaryKey).findFirst();
		if (primaryKey.isPresent()) return parameters + ", PRIMARY KEY (" + primaryKey.get().name + ")";
		return parameters;
	}
	
	public static <T> void setUpdatable(List<Column<T>> columns, String tableName, String determinantColumnName, int determinantColumnType, Function<T, Object> determinantSupplier) {
		columns.stream().filter(Column::isUpdatable).forEach(column -> column.setSQLUpdater(new SQLUpdater<>() {
			private OlympaStatement updateStatement = new OlympaStatement("UPDATE " + tableName + " SET " + column.getName() + " = ? WHERE (" + determinantColumnName + " = ?)");
			@Override
			public void update(T object, Object sqlObject, int sqlType) throws SQLException {
				PreparedStatement statement = updateStatement.getStatement();
				statement.setObject(1, sqlObject, sqlType);
				statement.setObject(2, determinantSupplier.apply(object), determinantColumnType);
				statement.executeUpdate();
			}
		}));
	}
	
}
