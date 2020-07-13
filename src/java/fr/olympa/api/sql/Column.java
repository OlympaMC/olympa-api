package fr.olympa.api.sql;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Column {
	
	private final String name;
	private final String type;
	private final boolean primaryKey;
	
	public Column(String name, String type) {
		this(name, type, false);
	}
	
	public Column(String name, String type, boolean primaryKey) {
		this.name = "`" + name + "`";
		this.type = type;
		this.primaryKey = primaryKey;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public boolean isPrimaryKey() {
		return primaryKey;
	}

	@Override
	public String toString() {
		return name + " " + type;
	}
	
	public static String toParameters(List<Column> columns) {
		String parameters = columns.stream().map(Column::toString).collect(Collectors.joining(", "));
		Optional<Column> primaryKey = columns.stream().filter(Column::isPrimaryKey).findFirst();
		if (primaryKey.isPresent()) return parameters + ", PRIMARY KEY (" + primaryKey.get().name + ")";
		return parameters;
	}
	
}
