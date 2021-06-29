package fr.olympa.api.common.sql;

import java.sql.Connection;
import java.sql.SQLException;

public interface DatabaseConnection {

	boolean isConnected() throws SQLException;

	Connection getConnection() throws SQLException;

	boolean connect();

	boolean close();
}
