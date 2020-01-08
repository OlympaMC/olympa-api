package fr.olympa.api.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.Executors;

import com.mysql.jdbc.Driver;

public class DbConnection {

	final DbCredentials dbcredentials;
	Connection connection;

	public DbConnection(DbCredentials dbcredentials) {
		this.dbcredentials = dbcredentials;
	}

	public boolean close() {
		try {
			if (this.connection != null && !this.connection.isClosed()) {
				this.connection.close();
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	public boolean connect() {
		try {
			Class.forName("org.mariadb.jdbc.Driver");
			Driver.class.getName();
			this.connection = DriverManager.getConnection(this.dbcredentials.toURI(), this.dbcredentials.getUser(), this.dbcredentials.getPassword());
			this.connection.setNetworkTimeout(Executors.newSingleThreadExecutor(), 28800);
			return !this.connection.isClosed();
		} catch (final SQLException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		return false;
	}

	public Connection getConnection() throws SQLException {
		if (this.connection != null && !this.connection.isClosed()) {
			return this.connection;
		}
		this.connect();
		return this.connection;
	}
}
