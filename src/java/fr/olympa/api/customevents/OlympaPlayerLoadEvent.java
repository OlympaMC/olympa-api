package fr.olympa.api.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.SpigotUtils;

public class OlympaPlayerLoadEvent extends PlayerEvent {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final private OlympaPlayer olympaPlayer;

	private String joinMessage;

	public OlympaPlayerLoadEvent(Player who, OlympaPlayer olympaPlayer) {
		super(who);
		this.olympaPlayer = olympaPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String getJoinMessage() {
		return SpigotUtils.color(this.joinMessage);
	}

	public OlympaPlayer getOlympaPlayer() {
		return this.olympaPlayer;
	}

	/**
	 *
	 * @param joinMessage %group %prefix %name are variables
	 */
	public void setJoinMessage(String joinMessage) {
		this.joinMessage = SpigotUtils.color(joinMessage
				.replaceAll("%group", this.olympaPlayer.getGroup().getName())
				.replaceAll("%prefix", this.olympaPlayer.getGroup().getPrefix())
				.replaceAll("%name", this.player.getName()));
	}
}