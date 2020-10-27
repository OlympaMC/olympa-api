package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.ServerStatus;

public abstract class OlympaSpigot extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected ServerStatus status;
	private INametagApi nameTagApi;

	@Override
	public Connection getDatabase() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public INametagApi getNameTagApi() {
		return nameTagApi;
	}
	
	public abstract AfkHandler getAfkHandler();

	public abstract IProtocolSupport getProtocolSupport();

	@Override
	public ServerStatus getStatus() {
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
	public void setStatus(ServerStatus status) {
		this.status = status;
	}
}
