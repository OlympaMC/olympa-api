package fr.olympa.api.common.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import fr.olympa.api.common.sql.statement.OlympaStatement;

public class SQLColumn<T> {

	private final String name;
	private final String type;
	private final int sqlType;
	private boolean isDefault;
	private boolean allowNull = false;
	private Function<T, Object> getSqlObject;
	private boolean updatable = false;

	private SQLUpdater<T> sqlUpdater;
	private SQLSelector<T> sqlSelector;
	private OlympaStatement selectStatement;

	public SQLColumn(String name, String type, int sqlType) {
		this.name = "`" + name + "`";
		this.type = type.toUpperCase();
		this.sqlType = sqlType;
		this.isDefault = type.contains("AUTO_INCREMENT") || type.contains("DEFAULT") || type.contains("NULL") && !type.contains("NOT NULL");
	}

	public String getName() {
		return name;
	}

	public String getCleanName() {
		return name.replace("`", "");
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

	public SQLColumn<T> allowNull() {
		this.allowNull = true;
		return this;
	}

	public boolean canBeNullable() {
		return allowNull;
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
		if (!updatable)
			throw new IllegalStateException("Cannot assign SQL Updater to non-updatable column.");
		this.sqlUpdater = statementCreation;
	}

	public void setSQLSelector(SQLSelector<T> statementCreation) {
		this.sqlSelector = statementCreation;
	}

	OlympaStatement getSelectStatement(SQLTable<T> table) {
		if (selectStatement == null)
			selectStatement = new OlympaStatement("SELECT * FROM " + table.getName() + " WHERE (" + name + " = ?)");
		return selectStatement;
	}

	@Deprecated // use async for performances
	public void updateValue(T object, Object sqlObject) throws SQLException {
		if (sqlUpdater != null)
			sqlUpdater.update(object, sqlObject, sqlType);
		else
			throw new IllegalAccessError(String.format("Cannot update value of column %s, it is non-updatable.", this));
	}

	public void updateAsync(T object, Object sqlObject, Runnable successCallback, Consumer<SQLException> failCallback) {
		if (sqlUpdater != null)
			sqlUpdater.updateAsync(object, sqlObject, sqlType, successCallback, failCallback);
		else
			throw new IllegalAccessError(String.format("Cannot update value of column %s, it is non-updatable.", this));
	}

	public List<T> select(Object sqlObject, String... specifiedColumnsReturned) throws SQLException, IllegalAccessException {
		if (sqlSelector != null)
			return sqlSelector.select(sqlObject, specifiedColumnsReturned);
		throw new IllegalAccessError("sqlSelector is null, check if you have linked correctly to a SQLTable class.");
	}

	public ResultSet selectBasic(Object sqlObject, String... specifiedColumnsReturned) throws SQLException {
		if (sqlSelector != null)
			return sqlSelector.selectBasic(sqlObject, specifiedColumnsReturned);
		throw new IllegalAccessError("sqlSelector is null, check if you have linked correctly to a SQLTable class.");
	}

	@Override
	public String toString() {
		return name + " " + type;
	}

	public interface SQLUpdater<T> {
		void update(T object, Object sqlObject, int sqlType) throws SQLException;

		void updateAsync(T object, Object sqlObject, int sqlType, Runnable successCallback, Consumer<SQLException> failCallback);
	}

	public interface SQLSelector<T> {
		List<T> select(Object sqlObject, String... specifiedColumnsReturned) throws SQLException, IllegalAccessException;

		ResultSet selectBasic(Object sqlObject, String... specifiedColumnsReturned) throws SQLException;
	}

	public static <T> String toParameters(List<SQLColumn<T>> columns) {
		String parameters = columns.stream().map(SQLColumn::toString).collect(Collectors.joining(", "));
		Optional<SQLColumn<T>> primaryKey = columns.stream().filter(SQLColumn::isPrimaryKey).findFirst();
		if (primaryKey.isPresent())
			return parameters + ", PRIMARY KEY (" + primaryKey.get().name + ")";
		return parameters;
	}

}
