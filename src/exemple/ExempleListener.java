package exemple;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.tristiisch.olympa.api.customevents.AsyncOlympaPlayerLoadEvent;
import fr.tristiisch.olympa.api.objects.OlympaPlayer;
import fr.tristiisch.olympa.api.permission.OlympaAccountObject;
import fr.tristiisch.olympa.api.permission.OlympaPermission;

public class ExempleListener implements Listener {

	@EventHandler
	public void onOlympaPlayerLoad(AsyncOlympaPlayerLoadEvent event) {
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		// ==
		olympaPlayer = new OlympaAccountObject(event.getPlayer().getUniqueId()).getFromCache();

		if (OlympaPermission.BAN_COMMAND.hasPermission(olympaPlayer)) {
			// si le joueur a la permission d'utiliser la commande /ban alors ...
		}

	}
}
