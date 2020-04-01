package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.maintenance.MaintenanceStatus;

public interface OlympaCoreInterface {

	Connection getDatabase() throws SQLException;

	MaintenanceStatus getStatus();

	void setStatus(MaintenanceStatus status);
}
