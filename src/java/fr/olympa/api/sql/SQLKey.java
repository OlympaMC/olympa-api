package fr.olympa.api.sql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map.Entry;
import java.util.function.Function;

public class SQLKey {

	public interface SetFunction<T, R> {
		R apply(T t) throws SQLException;
	}

	Function<?, ?> set;
	SetFunction<Entry<ResultSet, String>, ?> get;
	boolean nullable = false;

	// hard with the i of PreparedStatement.setObject(i, this)
	//	public SQLKey(Function<Entry<PreparedStatement, ?>, ?> get) {
	//		this.get = get;
	//	}

	public SQLKey(Function<?, ?> set) {
		this.set = set;
	}

	public SQLKey(Function<?, ?> set, SetFunction<Entry<ResultSet, String>, ?> get) {
		this.get = get;
		this.set = set;
	}

	public SQLKey(Function<?, ?> set, SetFunction<Entry<ResultSet, String>, ?> get, boolean nullable) {
		this.get = get;
		this.set = set;
		this.nullable = nullable;
	}

}
