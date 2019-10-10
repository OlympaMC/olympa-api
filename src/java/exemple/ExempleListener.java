package exemple;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.tristiisch.olympa.api.customevents.AsyncOlympaPlayerLoadEvent;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaPermission;
import fr.tristiisch.olympa.api.provider.AccountProvider;

public class ExempleListener implements Listener {

	@EventHandler
	public void onOlympaPlayerLoad(AsyncOlympaPlayerLoadEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		// ==
		olympaPlayer = AccountProvider.get(event.getPlayer());

		if (OlympaPermission.CHAT_COMMAND.hasPermission(olympaPlayer)) {
			// si le joueur a la permission d'utiliser la commande /chat alors ...
		}

	}
}
