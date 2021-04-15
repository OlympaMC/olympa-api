package fr.olympa.api.trades;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import fr.olympa.api.economy.OlympaMoney;
import fr.olympa.api.editor.Editor;
import fr.olympa.api.editor.TextEditor;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.api.utils.spigot.SpigotUtils;

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
	private Map<Player, Editor> selectMoney = new HashMap<Player, Editor>();
	
	private double tradeRange = -1;
	private boolean canTradeMoney = true;
	
	public TradesManager(OlympaAPIPlugin plugin, double tradeRange) {
		this(plugin, tradeRange, true);
	}
	
	public TradesManager(OlympaAPIPlugin plugin, double tradeRange, boolean canTradeMoney) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new TradeCommand<T>(plugin, this).register();
		
		this.canTradeMoney = canTradeMoney;
		this.tradeRange = tradeRange;
		
		plugin.getLogger().info("§aTrades manager enabled.");
	}
	
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e) {
		if (e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player))
			return;
		
		T p = AccountProvider.<T>get(e.getWhoClicked().getUniqueId());
		trades.stream().filter(trade -> trade.containsPlayer(p)).findAny().ifPresent(trade -> trade.click(e, p));
	}
	 
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		if (e.getPlayer().getType() != EntityType.PLAYER)
			return;
		
		if (selectMoney.containsKey(e.getPlayer()))
			return;	
		
		trades.stream().filter(trade -> trade.containsPlayer(
				AccountProvider.get(e.getPlayer().getUniqueId()))).findAny().ifPresent(trade -> trade.endTrade(false));
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent e) {
		if (SpigotUtils.isSameLocation(e.getFrom(), e.getTo()))
			return;
		
		if (selectMoney.containsKey(e.getPlayer()))
			selectMoney.remove(e.getPlayer()).chat("cancel");
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerTeleportEvent e) {
		trades.stream().filter(trade -> 
				trade.containsPlayer(AccountProvider.get(e.getPlayer().getUniqueId()))).findAny().ifPresent(trade -> trade.endTrade(false));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		trades.stream().filter(trade -> 
		trade.containsPlayer(AccountProvider.get(e.getPlayer().getUniqueId()))).findAny().ifPresent(trade -> trade.endTrade(false)); 
	}
	
	
	
	
	void openMoneySelectionFor(T p, UniqueTradeManager<T> trade) {
		if (!canTradeMoney())
			return;

		
		addMoney(p, trade.getTradeGuiOf(p).getPlayerMoney());
		trade.selectMoney(p, 0);

		Prefix.DEFAULT_GOOD.sendMessage(p.getPlayer(), "Sélectionnez l'argent à ajouter à l'échange : ");
		
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
				OlympaCore.getInstance().getTask().runTaskLater(() -> trade.endTrade(false), 20 * 30)));*/
		
	}
	
	
	
	
	
	
	public boolean canTradeMoney() {
		return canTradeMoney;
	}
	
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
	}
	
	String getMoneySymbol() {
		return OlympaMoney.OMEGA;
	}
	
	
	public void startTrade(T p1, T p2) {
		if (!p1.getTradeBag().isEmpty()) {
			Prefix.BAD.sendMessage(p1.getPlayer(), "Tu dois d'abord vider ton sac ! Fais /trade collect pour récupérer tes objets.");
			Prefix.BAD.sendMessage(p2.getPlayer(), "%s n'est pas encore prêt à démarrer un échange.", p1.getName());
			
		} else if (!p2.getTradeBag().isEmpty()) {
			Prefix.BAD.sendMessage(p2.getPlayer(), "Tu dois d'abord vider ton sac ! Fais /trade collect pour récupérer tes objets.");
			Prefix.BAD.sendMessage(p1.getPlayer(), "%s n'est pas encore prêt à démarrer un échange.", p2.getName());
		
		}else if (tradeRange == -1 || (p1.getPlayer().getWorld().getUID().equals(p2.getPlayer().getWorld().getUID()) && p1.getPlayer().getLocation().distance(p2.getPlayer().getLocation()) < tradeRange)) {
			Prefix.BAD.sendMessage(p1.getPlayer(), "Tu es trop loin de %s pour commencer l'échange.", p2.getName());
			Prefix.BAD.sendMessage(p2.getPlayer(), "Tu es trop loin de %s pour commencer l'échange.", p1.getName());
			
		}else if (trades.stream().anyMatch(trade -> trade.containsPlayer(p1))) {
			Prefix.BAD.sendMessage(p2.getPlayer(), "%s est déjà en échange, réessaies dans quelques minutes.", p1.getName());
			
		}else if (trades.stream().anyMatch(trade -> trade.containsPlayer(p2))) {
			Prefix.BAD.sendMessage(p1.getPlayer(), "%s est déjà en échange, réessaies dans quelques minutes.", p2.getName());
			
		}else
			trades.add(new UniqueTradeManager<T>(this, p1, p2));
	}
	
	public boolean isInTrade(T p) {
		return trades.stream().anyMatch(trade -> trade.containsPlayer(p));
	}
	
	public UniqueTradeManager<T> getTradeOf(T p){
		return trades.stream().filter(trade -> trade.containsPlayer(p)).findAny().orElse(null);
	}

	void unregister(UniqueTradeManager<T> trade) {
		trade.getPlayers().forEach(p -> {
			if (selectMoney.containsKey(p.getPlayer()))
				selectMoney.remove(p.getPlayer()).chat("cancel");
		});
	}
}




