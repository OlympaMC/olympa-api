package exemple;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import fr.olympa.api.customevents.OlympaPlayerLoadEvent;
import fr.olympa.api.customevents.PlayerNameTagEditEvent;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.scoreboard.tab.Nametag;
import fr.olympa.core.spigot.OlympaCore;

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

	@EventHandler
	public void onPlayerNameTagEdit(PlayerNameTagEditEvent event) {
		Player player = event.getPlayer();
		OlympaPlayer olympaPlayer = event.getOlympaPlayer();
		Nametag nameTag = event.getNameTag();
		nameTag.setPrefix("ADMIN"); // met Admin en prefix
		nameTag.appendSuffix("Gros BG"); // ajoute Gros BG en suffix
		// appel l'event, et applique le nametag par default et les autres
		OlympaCore.getInstance().getServer().getPluginManager().callEvent(new PlayerNameTagEditEvent(player, olympaPlayer, null, null));
	}

}
