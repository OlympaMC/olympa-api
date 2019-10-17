package exemple;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;

public class ExempleListener implements Listener {

	@EventHandler
	public void onOlympaPlayerLoad(OlympaPlayerLoadEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		// ==
		olympaPlayer = AccountProvider.get(event.getPlayer());

		if (ExemplePermissions.EXEMPLE_COMMAND.hasPermission(olympaPlayer)) {
			// si le joueur a la permission d'utiliser la commande /exemple alors ...
		}

	}

}
