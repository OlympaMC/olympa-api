package exemple;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.common.provider.AccountProvider;
import fr.olympa.api.spigot.customevents.OlympaPlayerLoadEvent;

public class ExempleListener implements Listener {

	@EventHandler
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		// ==
		olympaPlayer = AccountProvider.get(event.getPlayer().getUniqueId());

		if (ExemplePermissions.EXEMPLE_COMMAND.hasPermission(olympaPlayer)) {
			// si le joueur a la permission d'utiliser la commande /exemple alors ...
		}

	}
}
