package fr.olympa.api.spigot.trades;

import java.util.HashSet;
import java.util.Set;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;

import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Prefix;

/**
 * Instantiate this class to enable trades. Command /trade is registered internally
 *
 * @param <T> OlympaPlayer implementation for current plugin
 */
public class TradesManager<T extends TradePlayerInterface> implements Listener {
	
	/*private Function<T, Runnable> itemBagReminderSupplier = p -> 
		() -> Prefix.DEFAULT_BAD.sendMessage(p.getPlayer(), "Tu n'avais pas assez de place dans ton inventaire, certains objets n'ont pas pu y être placés. Fais /A DEFINIR pour les récupérer.\n§4Tu as jusqu'à ce soir, demain ils seront perdus !");
	
	private Map<Long, ItemStack[]> itemBags = new HashMap<Long, ItemStack[]>();
	private Map<Long, Integer> itemBagsTasks = new HashMap<Long, Integer>();*/

	private Set<UniqueTradeManager<T>> trades = new HashSet<UniqueTradeManager<T>>();

	//private Map<Player, UniqueTradeManager<T>> selectMoney = new HashMap<Player, UniqueTradeManager<T>>();
	//private Map<Player, Editor> selectMoney = new HashMap<Player, Editor>();
	
	private double tradeRange = -1;
	
	public TradesManager(OlympaAPIPlugin plugin, double tradeRange) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new TradeCommand<T>(plugin, this).register();
		
		this.tradeRange = tradeRange;
		
		plugin.getLogger().info("§aTrades manager enabled.");
	}
	
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e) {
		if (e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player))
			return;
		
		T p = AccountProviderAPI.getter().get(e.getWhoClicked().getUniqueId());
		trades.forEach(trade -> trade.click(e, p));
	}
	 
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		if (e.getPlayer().getType() != EntityType.PLAYER)
			return;
		
		T p = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());
		trades.forEach(trade -> trade.hasClosedInventory(p));
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent e) {
		if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo()))
			return;
		
		T p = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());
		trades.forEach(trade -> {if (trade.containsPlayer(p)) trade.endTrade(false);});
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerTeleportEvent e) {
		T p = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());
		trades.forEach(trade -> {if (trade.containsPlayer(p)) trade.endTrade(false);});
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		T p = AccountProviderAPI.getter().get(e.getPlayer().getUniqueId());
		trades.forEach(trade -> {if (trade.containsPlayer(p)) trade.endTrade(false);}); 
	}
	
	
	
	/*
	void openMoneySelectionFor(T p, UniqueTradeManager<T> trade) {
		if (!canTradeMoney())
			return;

		trade.selectMoney(p, 0);

		Prefix.DEFAULT_GOOD.sendMessage(p.getPlayer(), "§eSélectionnez l'argent à ajouter à l'échange (§7\"cancel\" pour annuler) §e: ");
		
		selectMoney.put(p.getPlayer(), new TextEditor<Double>(p.getPlayer(), d -> {
			trade.selectMoney(p, d);
			trade.openFor(p);
		}, () -> trade.endTrade(false), false, (player, msg) -> {
			try {
				return Double.valueOf(msg);	
			}catch (Exception ex) {
				return 0d;
			}
		}).enterOrLeave());

		p.getPlayer().closeInventory();
		/*selectMoney.put(p.getPlayer(), new AbstractMap.SimpleEntry<UniqueTradeManager<T>, Integer>(trade, 
				OlympaCore.getInstance().getTask().runTaskLater(() -> trade.endTrade(false), 20 * 30)));
		
	}*/
	
	/*
	boolean hasMoney(T p, double money) {
		return canTradeMoney() ? p.getGameMoney().has(money) : false;
	}
	
	void addMoney(T p, double amount) {
		if (canTradeMoney())
			p.getGameMoney().give(amount);
	}
	
	void removeMoney(T p, double amount) {
		if (canTradeMoney())
			p.getGameMoney().withdraw(amount);
	}*/
	
	
	public void startTrade(T p1, T p2) {
		Player player1 = (Player) p1.getPlayer();
		Player player2 = (Player) p2.getPlayer();
		if (!p1.getTradeBag().isEmpty()) {
			Prefix.BAD.sendMessage(player1, "Tu dois d'abord vider ton sac ! Fais /trade collect pour récupérer tes objets.");
			Prefix.BAD.sendMessage(player2, "%s n'est pas encore prêt à démarrer un échange.", p1.getName());
			
		} else if (!p2.getTradeBag().isEmpty()) {
			Prefix.BAD.sendMessage(player2, "Tu dois d'abord vider ton sac ! Fais /trade collect pour récupérer tes objets.");
			Prefix.BAD.sendMessage(player1, "%s n'est pas encore prêt à démarrer un échange.", p2.getName());
		
		}else if (tradeRange == -1 || (player1.getWorld().getUID().equals(player2.getWorld().getUID()) && player1.getLocation().distance(player2.getLocation()) > tradeRange)) {
			Prefix.BAD.sendMessage(player1, "Tu es trop loin de %s pour commencer l'échange.", p2.getName());
			Prefix.BAD.sendMessage(player2, "Tu es trop loin de %s pour commencer l'échange.", p1.getName());
			
		}else if (trades.stream().anyMatch(trade -> trade.containsPlayer(p1))) {
			Prefix.BAD.sendMessage(player2, "%s est déjà en échange, réessaies dans quelques minutes.", p1.getName());
			
		}else if (trades.stream().anyMatch(trade -> trade.containsPlayer(p2))) {
			Prefix.BAD.sendMessage(player1, "%s est déjà en échange, réessaies dans quelques minutes.", p2.getName());
			
		}else {
			UniqueTradeManager<T> trade = new UniqueTradeManager<T>(p1, p2);
			trades.add(trade);
			trade.observe("cancel_trade", () -> trades.remove(trade));
		}
			
	}
	
	public boolean isInTrade(T p) {
		return trades.stream().anyMatch(trade -> trade.containsPlayer(p));
	}
	
	public UniqueTradeManager<T> getTradeOf(T p){
		return trades.stream().filter(trade -> trade.containsPlayer(p)).findAny().orElse(null);
	}

	/*
	boolean unregister(UniqueTradeManager<?> trade) {
		return trades.remove(trade);
	}*/
}




