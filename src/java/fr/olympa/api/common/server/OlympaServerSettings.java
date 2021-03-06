package fr.olympa.api.common.server;

import org.bukkit.Location;

public class OlympaServerSettings {

	private final static OlympaServerSettings instance = new OlympaServerSettings();

	public static OlympaServerSettings getInstance() {
		return instance;
	}

	private Location spawn = null;
	private boolean chatSlow = true;
	private boolean chatMute = false;
	private int timeCooldown = 2;
	private int blockCaps = 5;
	private int maxCaps = 50;

	public int getBlockCaps() {
		return this.blockCaps;
	}

	public int getMaxCaps() {
		return this.maxCaps;
	}

	public Location getSpawn() {
		return this.spawn;
	}

	public int getTimeCooldown() {
		return this.timeCooldown;
	}

	public boolean isChatMute() {
		return this.chatMute;
	}

	public boolean isChatSlow() {
		return this.chatSlow;
	}

	public void setBlockCaps(int blockCaps) {
		this.blockCaps = blockCaps;
	}

	public void setChatMute(final boolean chatmute) {
		this.chatMute = chatmute;
	}

	public void setChatSlow(final boolean chatslow) {
		this.chatSlow = chatslow;
	}

	public void setMaxCaps(int maxCaps) {
		this.maxCaps = maxCaps;
	}

	public void setSpawn(Location spawn) {
		this.spawn = spawn;
	}

	public void setTimeCooldown(int timecooldown) {
		this.timeCooldown = timecooldown;
	}
}
