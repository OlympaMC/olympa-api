package fr.olympa.api.spigot.feedback;

import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;

public class FeedbackEntry {

	public int id;
	
	public long date;
	
	public OlympaPlayerInformations owner;
	
	public FeedbackType type;
	
	public String description;
	
	public OlympaServer server;
	
	public ServerInfoAdvanced serverInfo;
	
}
