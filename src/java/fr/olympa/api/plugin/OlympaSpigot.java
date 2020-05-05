package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.hook.ProtocolAction;
import fr.olympa.api.maintenance.MaintenanceStatus;
import fr.olympa.api.scoreboard.tab.INametagApi;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected MaintenanceStatus status;
	private INametagApi nameTagApi;

	@Override
	public Connection getDatabase() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public INametagApi getNameTagApi() {
		return nameTagApi;
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
