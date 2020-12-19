package fr.olympa.api.sql;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang.Validate;

public class SQLColumn<T> {
	
	private final String name;
	private final String type;
	private final int sqlType;
	private boolean isDefault;
	private Function<T, Object> getSqlObject;
	private boolean updatable = false;
	
	private SQLUpdater<T> sqlUpdater;
	
	public SQLColumn(String name, String type, int sqlType) {
		this.name = "`" + name + "`";
		this.type = type.toUpperCase();
		this.sqlType = sqlType;
		this.isDefault = type.contains("AUTO_INCREMENT") || type.contains("DEFAULT") || (type.contains("NULL") && !type.contains("NOT NULL"));
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public int getSQLType() {
		return sqlType;
	}
	
	public boolean isDefault() {
		return isDefault;
	}
	
	public boolean isNotDefault() {
		return !isDefault;
	}
	
	public SQLColumn<T> setNotDefault() {
		this.isDefault = false;
		return this;
	}
	
	public String toDeclaration() {
		return name + " " + type;
	}
	
	public SQLColumn<T> setPrimaryKey(Function<T, Object> getSqlObject) {
		this.getSqlObject = getSqlObject;
		return this;
	}
	
	public boolean isPrimaryKey() {
		return getSqlObject != null;
	}
	
	public Object getPrimaryKeySQLObject(T object) {
		return getSqlObject.apply(object);
	}

	public SQLColumn<T> setUpdatable() {
		this.updatable = true;
		return this;
	}
	
	public boolean isUpdatable() {
		return updatable;
	}
	
	public void setSQLUpdater(SQLUpdater<T> statementCreation) {
		Validate.isTrue(updatable, "Cannot assign SQL Updater to non-updatable column.");
		this.sqlUpdater = statementCreation;
	}
	
	public void updateValue(T object, Object sqlObject) throws SQLException {
		if (sqlUpdater != null) sqlUpdater.update(object, sqlObject, sqlType);
	}
	
	@Override
	public String toString() {
		return name + " " + type;
	}
	
	@FunctionalInterface
	public interface SQLUpdater<T> {
		void update(T object, Object sqlObject, int sqlType) throws SQLException;
	}
	
	public static <T> String toParameters(List<SQLColumn<T>> columns) {
		String parameters = columns.stream().map(SQLColumn::toString).collect(Collectors.joining(", "));
		Optional<SQLColumn<T>> primaryKey = columns.stream().filter(SQLColumn::isPrimaryKey).findFirst();
		if (primaryKey.isPresent()) return parameters + ", PRIMARY KEY (" + primaryKey.get().name + ")";
		return parameters;
	}
	
}
