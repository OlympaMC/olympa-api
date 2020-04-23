package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.objects.ProtocolAction;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected MaintenanceStatus status;

	@Override
	public Connection getDatabase() throws SQLException {
		throw new UnsupportedOperationException();
	}

	public abstract ProtocolAction getProtocolSupport();

	@Override
	public MaintenanceStatus getStatus() {
		return status;
	}

	@Override
	public void onDisable() {
		super.onDisable();
	}

	@Override
	public void onEnable() {
		super.onEnable();
	}

	@Override
	public void setStatus(MaintenanceStatus status) {
		this.status = status;
	}
}
