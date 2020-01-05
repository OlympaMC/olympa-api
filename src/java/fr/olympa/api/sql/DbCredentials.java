package fr.olympa.api.sql;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class DbCredentials {

	private String host;
	private String user = null;
	private String password;
	private int port = 3306;
	private String database;

	public DbCredentials(FileConfiguration config) {
		ConfigurationSection databaseDefault = config.getConfigurationSection("database.default");
		if (databaseDefault == null) {
			return;
		}
		this.host = databaseDefault.getString("host");
		this.user = databaseDefault.getString("user");
		this.password = databaseDefault.getString("password");
		this.database = databaseDefault.getString("database");
		int configInt = databaseDefault.getInt("port");
		if (configInt != 0) {
			this.port = configInt;
		}
	}

	public DbCredentials(String host, String user, String password) {
		this.host = host;
		this.user = user;
		this.password = password;
	}

	public DbCredentials(String host, String user, String password, String dbName, int port) {
		this.host = host;
		this.user = user;
		this.password = password;
		this.database = dbName;
		this.port = port;
	}

	public String getDatabase() {
		return this.database;
	}

	public String getHost() {
		return this.host;
	}

	public String getPassword() {
		return this.password;
	}

	public int getPort() {
		return this.port;
	}

	public String getUser() {
		return this.user;
	}

	public String toURI() {
		StringBuilder sb = new StringBuilder();
		sb.append("jdbc:mariadb://").append(this.host).append(":").append(this.port).append("/").append(this.database).append("?autoReconnect=true");
		return sb.toString();
	}
}
