package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.sql.DbConnection;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected DbConnection database = null;
	protected MaintenanceStatus status;

	public void onDisable() {
		super.onDisable();
	}

	public void onEnable() {
		super.onEnable();
	}

	@Override
	public Connection getDatabase() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public MaintenanceStatus getStatus() {
		return this.status;
	}

	@Override
	public void setStatus(MaintenanceStatus status) {
		this.status = status;
	}

	private void setupDatabase() {
	}
}
