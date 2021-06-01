package fr.olympa.api.bungee.customevent;

import fr.olympa.api.common.groups.OlympaGroup;
import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.customevents.AsyncOlympaPlayerChangeGroupEvent.ChangeType;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class BungeeOlympaGroupChangeEvent extends Event {

	final private OlympaPlayer olympaPlayer;
	final private ProxiedPlayer player;
	final private OlympaGroup groupChanged;
	final private long timestamp;
	final private ChangeType state;

	public BungeeOlympaGroupChangeEvent(ProxiedPlayer player, OlympaPlayer olympaPlayer, OlympaGroup groupChanged, long timestamp, ChangeType state) {
		super();
		this.player = player;
		this.olympaPlayer = olympaPlayer;
		this.groupChanged = groupChanged;
		this.state = state;
		this.timestamp = timestamp;
	}

	public OlympaGroup getGroupChanged() {
		return groupChanged;
	}

	@SuppressWarnings("unchecked")
	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return (T) olympaPlayer;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public ChangeType getState() {
		return state;
	}

	public long getTimestamp() {
		return timestamp;
	}
}