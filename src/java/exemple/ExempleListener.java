package exemple;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.PlayerMoveBlockEvent;
import fr.olympa.api.customevents.PlayerMoveBlockXZEvent;
import fr.olympa.api.customevents.PlayerMoveBlockYEvent;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;

public class ExempleListener implements Listener {

	@EventHandler
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		// ==
		olympaPlayer = AccountProvider.get(event.getPlayer().getUniqueId());

		if (OlympaAPIPermission.EXEMPLE_COMMAND.hasPermission(olympaPlayer)) {
			// si le joueur a la permission d'utiliser la commande /exemple alors ...
		}

	}

	@EventHandler
	public void onPlayerMoveBlock(PlayerMoveBlockEvent event) {
		// Quand un joueur change de block en hauteur (Y) et peux être sur le côté
	}

	@EventHandler
	public void onPlayerMoveBlock(PlayerMoveBlockXZEvent event) {
		// Quand un joueur change de block sur le côté (XZ) et peux être en hauteur
	}

	@EventHandler
	public void onPlayerMoveBlock(PlayerMoveBlockYEvent event) {
		// Quand un joueur change de block
	}

}
