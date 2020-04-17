package fr.olympa.api.customevents;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.ColorUtils;

public class OlympaPlayerLoadEvent extends Event {

	public static final HandlerList handlers = new HandlerList();

	public static HandlerList getHandlerList() {
		return handlers;
	}

	final private OlympaPlayer olympaPlayer;
	final private Player player;

	private String joinMessage;

	public OlympaPlayerLoadEvent(Player who, OlympaPlayer olympaPlayer, boolean async) {
		super(async);
		this.player = who;
		this.olympaPlayer = olympaPlayer;
	}

	@Override
	public HandlerList getHandlers() {
		return handlers;
	}

	public String getJoinMessage() {
		return this.joinMessage;
	}

	public Player getPlayer() {
		return player;
	}

	public <T extends OlympaPlayer> T getOlympaPlayer() {
		return (T) this.olympaPlayer;
	}

	/**
	 *
	 * @param joinMessage %group %prefix %name are variables
	 */
	public void setJoinMessage(String joinMessage) {
		if (joinMessage == null) {
			this.joinMessage = null;
		} else {
			this.joinMessage = ColorUtils.color(joinMessage
					.replaceAll("%group", this.olympaPlayer.getGroup().getName())
					.replaceAll("%prefix", this.olympaPlayer.getGroup().getPrefix())
					.replaceAll("%name", this.player.getName()));
		}
	}
}