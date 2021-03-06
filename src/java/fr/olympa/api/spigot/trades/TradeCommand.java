package fr.olympa.api.spigot.trades;

import org.bukkit.entity.Player;

import com.google.common.collect.HashMultimap;

import fr.olympa.api.common.command.complex.Cmd;
import fr.olympa.api.common.command.complex.CommandContext;
import fr.olympa.api.common.permission.list.OlympaAPIPermissionsSpigot;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.command.ComplexCommand;
import fr.olympa.api.utils.Prefix;

/**
 * The trade command is registered by the module TradesManager, it should not be instanciated separately
 *
 */
public class TradeCommand<T extends TradePlayerInterface> extends ComplexCommand {

	private OlympaAPIPlugin plugin;
	private TradesManager<T> trades;
	
	private HashMultimap<Player, Player> map = HashMultimap.create();
	
	public TradeCommand(OlympaAPIPlugin plugin, TradesManager<T> trades) {
		super(plugin, "trade", "Échange des objets et de l'argent avec un autre joueur.", OlympaAPIPermissionsSpigot.TRADE_COMMAND);
		this.plugin = plugin;
		this.trades = trades;
	}
	
	@Cmd(args = "PLAYERS", min = 1, player = true, description = "Envoies ou acceptes une requête d'échange à un autre joueur")
	public void with(CommandContext cmd) {
		/*if (!trades.isEnabled()) {
			Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "Les échanges sont temporairement désactivés, réessaies plus tard.");
			return;
		}*/
		
		Player partner = cmd.getArgument(0);
		
		if (getPlayer().equals(partner))
			Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "Tu ne peux pas échanger avec toi-même...");
		
		else if (map.containsEntry(getPlayer(), partner))
			Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "Tu as déjà envoyé une demande à %s, patiente un peu avant d'en renvoyer une !", partner.getName());
		
		else if (map.remove(partner, getPlayer()))
			trades.startTrade(getOlympaPlayer(), AccountProviderAPI.getter().get(partner.getUniqueId()));
		
		else {
			map.put(getPlayer(), partner);
			plugin.getTask().runTaskLater(() -> {if (map.remove(getPlayer(), partner)) 
				Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "La demande d'échange envoyée à %s a expiré.", partner.getName());}
			, 1200);
			
			Prefix.DEFAULT_GOOD.sendMessage(getPlayer(), "Demande d'échange envoyée à %s.", partner.getName());
			Prefix.DEFAULT_GOOD.sendMessage(partner, "Demande d'échange reçue de %s, fais /trade with %s pour l'accepter.", getPlayer().getName(), getPlayer().getName());
		}
	}
	
	@SuppressWarnings("unchecked")
	@Cmd(player = true, description = "Récupère les objets que tu n'as pas pu récupérer de ton échange précédent")
	public void flushbag(CommandContext cmd) {
		((T)getOlympaPlayer()).getTradeBag().flushBag();
	}
	
	
}
