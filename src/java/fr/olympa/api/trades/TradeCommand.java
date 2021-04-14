package fr.olympa.api.trades;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.google.common.collect.HashMultimap;

import fr.olympa.api.command.complex.Cmd;
import fr.olympa.api.command.complex.CommandContext;
import fr.olympa.api.command.complex.ComplexCommand;
import fr.olympa.api.permission.OlympaAPIPermissions;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;

/**
 * The trade command is registered by the module TradesManager, it should not be instanciated separately
 *
 */
public class TradeCommand extends ComplexCommand {

	private OlympaAPIPlugin plugin;
	private TradesManager trades;
	
	private HashMultimap<Player, Player> map = HashMultimap.create();
	
	public TradeCommand(OlympaAPIPlugin plugin, TradesManager trades) {
		super(plugin, "trade", "Echange des objets et de l'argent avec un autre joueur", OlympaAPIPermissions.TRADE_COMMAND);
		this.plugin = plugin;
		this.trades = trades;
	}
	
	@Cmd(args = "PLAYERS", min = 1, description = "Envoies ou acceptes une requête d'échange à un autre joueur")
	public void trade(CommandContext cmd) {
		if (!trades.isEnabled()) {
			Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "Les échanges sont temporairement désactivés, réessaies plus tard.");
			return;
		}
		
		Player partner = cmd.getArgument(0);
		
		if (map.containsEntry(getPlayer(), partner))
			Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "Tu as déjà envoyé une demande à %s, patientes un peu avant d'en renvoyer une !", ((Player)partner).getName());
		
		else if (map.remove(partner, getPlayer()))
			trades.startTrade(getOlympaPlayer(), AccountProvider.get(partner.getUniqueId()));
		
		else {
			map.put(getPlayer(), partner);
			plugin.getTask().runTaskLater(() -> {if (map.remove(getPlayer(), partner)) 
				Prefix.DEFAULT_BAD.sendMessage(getPlayer(), "La demande d'échange à %s a expiré.", partner.getName());}
			, 1200);
			
			Prefix.DEFAULT_GOOD.sendMessage(getPlayer(), "Demande d'échange envoyée à %s.", partner.getName());
			Prefix.DEFAULT_GOOD.sendMessage(partner, "Demande d'échange reçue de %s, fais /trade %s pour l'accepter.", getPlayer().getName(), getPlayer().getName());
		}
	}
}