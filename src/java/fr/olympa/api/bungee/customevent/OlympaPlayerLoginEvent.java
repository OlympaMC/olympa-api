package fr.olympa.api.bungee.customevent;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.player.OlympaPlayer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Event;

public class OlympaPlayerLoginEvent extends Event {

	private boolean cancelled = false;
	private OlympaPlayer olympaPlayer;
	private ProxiedPlayer player;
	private TextComponent reason;

	public OlympaPlayerLoginEvent(OlympaPlayer olympaPlayer, ProxiedPlayer player) {
		this.olympaPlayer = olympaPlayer;
		this.player = player;
	}

	public boolean cancelIfNeeded() {
		if (!cancelled)
			return false;
		if (reason != null)
			player.disconnect(reason);
		else
			player.disconnect();
		return true;
	}

	@SuppressWarnings("deprecation")
	public String getIp() {
		return player.getAddress().getAddress().getHostAddress();
	}

	public OlympaPlayer getOlympaPlayer() {
		return olympaPlayer;
	}

	public ProxiedPlayer getPlayer() {
		return player;
	}

	public TextComponent getReason() {
		return reason;
	}

	public boolean isCancelled() {
		return cancelled;
	}

	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	public void setCancelReason(String reason) {
		this.reason = new TextComponent(TextComponent.fromLegacyText(ColorUtils.format(reason)));
		//		this.reason = BungeeUtils.connectScreen(reason);
	}

}
