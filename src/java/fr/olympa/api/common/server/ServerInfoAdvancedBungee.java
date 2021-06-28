package fr.olympa.api.common.server;

import java.util.ArrayList;
import java.util.Collection;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.core.bungee.OlympaBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerInfoAdvancedBungee extends ServerInfoAdvanced {

	public ServerInfoAdvancedBungee() {
		super();
	}

	public ServerInfoAdvancedBungee(OlympaBungee core) {
		super(core);
		ProxyServer server = core.getProxy();
		maxPlayers = server.getOnlineCount();
		Collection<ProxiedPlayer> playersSpigot = server.getPlayers();
		onlinePlayers = playersSpigot.size();
		players = new ArrayList<>();
		server.getPlayers().forEach(player -> {
			players.add(AccountProviderAPI.getter().getPlayerInformations(player.getUniqueId()));
		});
		serverFrameworkVersion = core.getProxy().getVersion();
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public void setPing(int ping) {
		this.ping = ping;
	}

	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public void setError(String error) {
		this.error = error;
	}

	public static ServerInfoAdvancedBungee fromJson(String string) {
		if (string == null || string.isBlank() || string.length() == 2 && string.equals("{}"))
			return null;
		return LinkSpigotBungee.getInstance().getGson().fromJson(string, ServerInfoAdvancedBungee.class);
	}

}
