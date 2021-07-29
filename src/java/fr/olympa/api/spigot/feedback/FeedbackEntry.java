package fr.olympa.api.spigot.feedback;

import org.bukkit.Location;

import fr.olympa.api.common.player.OlympaPlayerInformations;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.common.server.ServerInfoAdvanced;
import fr.olympa.api.spigot.utils.SpigotUtils;

public class FeedbackEntry {

	public int id;
	
	public long date;
	
	public OlympaPlayerInformations owner;
	
	public FeedbackStatus status;
	
	public FeedbackType type;
	
	public String description;
	
	public OlympaServer server;
	
	public ServerInfoAdvanced serverInfo;
	
	private Location position;
	
	private String worldName;
	
	public int getID() {
		return id;
	}
	
	public void setPosition(Location location) {
		this.position = location.clone();
		this.position.setWorld(null);
		this.worldName = location.getWorld().getName();
	}
	
	public void setPosition(String position) {
		this.position = SpigotUtils.convertStringToLocation(position);
		this.worldName = position.split(" ")[0];
	}
	
	public String getPositionString() {
		return worldName + " " + SpigotUtils.convertLocationToString(position);
	}
	
}
