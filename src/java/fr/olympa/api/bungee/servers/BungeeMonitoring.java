package fr.olympa.api.bungee.servers;

import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;

import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvancedBungee;
import net.md_5.bungee.api.config.ServerInfo;

public interface BungeeMonitoring {

	Map<Integer, ServerInfoAdvancedBungee> getServers(OlympaServer server);

	Map<OlympaServer, Map<Integer, ServerInfoAdvancedBungee>> getServersByType();

	Map<OlympaServer, Map<Integer, ServerInfoAdvancedBungee>> getServersByTypeWithBungee();

	ServerInfoAdvancedBungee getMonitor(ServerInfo server);

	Collection<ServerInfoAdvancedBungee> getServers();

	Collection<ServerInfoAdvancedBungee> getServersWithBungee();

	Stream<ServerInfoAdvancedBungee> getServersSorted();

	Map<ServerInfo, ServerInfoAdvancedBungee> getServersMap();

	void updateServer(ServerInfo serverInfo, boolean instantUpdate, Consumer<ServerInfo> sucess);

	void updateServer(ServerInfo serverInfo, OlympaServer olympaServer, ServerInfoAdvancedBungee info);

}