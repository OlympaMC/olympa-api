package fr.olympa.api.simplelistener;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import com.google.common.base.Function;

import fr.olympa.api.utils.Prefix;
import io.papermc.paper.event.player.AsyncChatEvent;

public class CancelActionListener implements Listener {

	Function<Player, Boolean> isIn;
	String tryToSpeak = "Le chat est désactivé.";
	String tryToSendCommand = "Impossible de faire une commande";
	String tryToMove = null;

	public CancelActionListener(Function<Player, Boolean> isIn, String tryTo) {
		this(isIn, tryTo, tryTo, tryTo);
	}

	public CancelActionListener(Function<Player, Boolean> isIn, String tryToSpeak, String tryToSendCommand, String tryToMove) {
		this(isIn);
		this.tryToSpeak = tryToSpeak;
		this.tryToSendCommand = tryToSendCommand;
		this.tryToMove = tryToMove;
	}

	public CancelActionListener(Function<Player, Boolean> isIn) {
		this.isIn = isIn;
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerChat(AsyncChatEvent event) {
		Player player = event.getPlayer();
		if (isIn.apply(player) && player.getGameMode() != GameMode.CREATIVE) {
			if (tryToSpeak != null)
				Prefix.DEFAULT_BAD.sendMessage(player, tryToSpeak);
			event.setCancelled(true);
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
		Player player = event.getPlayer();
		if (isIn.apply(player) && player.getGameMode() != GameMode.CREATIVE) {
			if (tryToSendCommand != null)
				Prefix.DEFAULT_BAD.sendMessage(player, tryToSendCommand);
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerTeleportEvent event) {
		Player player = event.getPlayer();
		if (isIn.apply(player) && player.getGameMode() != GameMode.CREATIVE && !event.getCause().equals(TeleportCause.PLUGIN)) {
			if (tryToMove != null)
				Prefix.DEFAULT_BAD.sendMessage(player, tryToMove);
			event.setTo(event.getFrom());
		}
	}

	@EventHandler
	public void onPlayerMove(PlayerMoveEvent event) {
		Player player = event.getPlayer();
		Location playerLoc = player.getLocation();
		Location toLoc = event.getTo();
		if (isIn.apply(player) && player.getGameMode() != GameMode.CREATIVE && (playerLoc.getX() != toLoc.getX() || playerLoc.getZ() != toLoc.getZ())) {
			if (tryToMove != null)
				Prefix.DEFAULT_BAD.sendMessage(player, tryToMove);
			event.setTo(event.getFrom());
		}
	}
}
