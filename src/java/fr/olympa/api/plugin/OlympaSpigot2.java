package fr.olympa.api.plugin;

import java.sql.Connection;
import java.sql.SQLException;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import fr.olympa.api.afk.AfkHandler;
import fr.olympa.api.hook.IProtocolSupport;
import fr.olympa.api.scoreboard.tab.INametagApi;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.server.ServerStatus;
import fr.olympa.api.vanish.IVanishApi;

public abstract class OlympaSpigot2 extends OlympaAPIPlugin implements OlympaCoreInterface {

	protected ServerStatus status;
	private INametagApi nameTagApi;
	private IVanishApi vanishApi;

	private OlympaServer olympaServer = OlympaServer.ALL;

	@Override
	public OlympaServer getOlympaServer() {
		return olympaServer;
	}

	@Override
	public void setOlympaServer(OlympaServer olympaServer) {
		this.olympaServer = olympaServer;
	}

	@Override
	public Connection getDatabase() throws SQLException {
		throw new UnsupportedOperationException();
	}

	@Override
	public IVanishApi getVanishApi() {
		return vanishApi;
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
		while (Bukkit.getOnlinePlayers().size() > 0) {
			for (Player p : Bukkit.getOnlinePlayers())
				p.kickPlayer("Server closed");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
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
