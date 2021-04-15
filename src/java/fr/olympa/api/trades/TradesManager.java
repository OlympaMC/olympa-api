package fr.olympa.api.trades;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import net.kyori.adventure.text.Component;

/**
 * Instantiate this class to enable trades. Command /trade is registered internally
 *
 * @param <T> OlympaPlayer implementation for current plugin
 */
public class TradesManager<T extends MoneyPlayerInterface> implements Listener {
	
	private Function<T, Runnable> itemBagReminderSupplier = p -> 
		() -> Prefix.DEFAULT_BAD.sendMessage(p.getPlayer(), "Tu n'avais pas assez de place dans ton inventaire, certains objets n'ont pas pu y être placés. Fais /A DEFINIR pour les récupérer.\n§4Tu as jusqu'à ce soir, demain ils seront perdus !");
	
	private Map<Long, ItemStack[]> itemBags = new HashMap<Long, ItemStack[]>();
	private Map<Long, Integer> itemBagsTasks = new HashMap<Long, Integer>();

	private Set<UniqueTradeManager<T>> trades = new HashSet<UniqueTradeManager<T>>();

	//private Map<Player, UniqueTradeManager<T>> selectMoney = new HashMap<Player, UniqueTradeManager<T>>();
	private Map<Player, Entry<UniqueTradeManager<T>, Integer>> selectMoney = new HashMap<Player, Map.Entry<UniqueTradeManager<T>,Integer>>();
	
	/*
	private BiFunction<T, Double, Boolean> hasMoney;
	private BiConsumer<T, Double> addMoney;
	private BiConsumer<T, Double> removeMoney;
	private String moneySymbol;*/
	
	private double tradeRange = -1;
	private boolean canTradeMoney = true;
	
	public TradesManager(OlympaAPIPlugin plugin, double tradeRange) {
		this(plugin, tradeRange, true);
	}
	
	public TradesManager(OlympaAPIPlugin plugin, double tradeRange, boolean canTradeMoney) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new TradeCommand(plugin, this).register();
		
		this.canTradeMoney = canTradeMoney;
		this.tradeRange = tradeRange;
		
		plugin.getLogger().info("§aTrades manager enabled.");
	}
	
	
	
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e) {
		if (e.getClickedInventory() == null || !(e.getWhoClicked() instanceof Player))
			return;
		
		/*if (selectMoneyTask.containsKey(e.getWhoClicked())) {
			
			if (e.getRawSlot() == 2 && trades.contains(selectMoney.get(e.getWhoClicked()))) {
				try {
					selectMoney.get(e.getWhoClicked()).selectMoney(AccountProvider.get(e.getWhoClicked().getUniqueId()), Double.valueOf((e.getInventory().getItem(2).getItemMeta().getDisplayName())));
				}catch(NumberFormatException ex) {
					selectMoney.get(e.getWhoClicked()).selectMoney(AccountProvider.get(e.getWhoClicked().getUniqueId()), 0);
				}
				
				OlympaCore.getInstance().getTask().cancelTaskById(selectMoneyTask.remove(e.getWhoClicked()));
				((Player)e.getWhoClicked()).closeInventory();
			}

			e.setCancelled(true);
			return;
		}*/
		
		T p = AccountProvider.<T>get(e.getWhoClicked().getUniqueId());
		trades.stream().filter(trade -> trade.containsPlayer(p)).findAny().ifPresent(trade -> trade.click(e, p));
	}
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onChat(PlayerChatEvent e) {
		if (selectMoney.containsKey(e.getPlayer())) {
			try {
				selectMoney.get(e.getPlayer()).getKey().selectMoney(AccountProvider.get(e.getPlayer().getUniqueId()), Double.valueOf(e.getMessage()));
			}catch(NumberFormatException ex) {
				selectMoney.get(e.getPlayer()).getKey().selectMoney(AccountProvider.get(e.getPlayer().getUniqueId()), 0);
			}
			
			OlympaCore.getInstance().getTask().cancelTaskById(selectMoney.get(e.getPlayer()).getValue());
			selectMoney.remove(e.getPlayer()).getKey().openFor(AccountProvider.get(e.getPlayer().getUniqueId()));
			e.setCancelled(true);
		}
	}
	 
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		if (e.getPlayer().getType() != EntityType.PLAYER)
			return;
		
		T p = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (selectMoney.containsKey(e.getPlayer()))
			return;	
		
		trades.stream().filter(trade -> trade.containsPlayer(p)).findAny().ifPresent(trade -> trade.endTrade(false));
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlock().equals(e.getTo().getBlock()))
			return;
		
		if (selectMoney.containsKey(e.getPlayer()))
			selectMoney.get(e.getPlayer()).getKey().endTrade(false);
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
	public void onTeleport(PlayerTeleportEvent e) {
		trades.stream().filter(trade -> 
				trade.containsPlayer(AccountProvider.get(e.getPlayer().getUniqueId()))).findAny().ifPresent(trade -> trade.endTrade(false));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		T op = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (itemBagsTasks.containsKey(op.getId()))
			OlympaCore.getInstance().getTask().cancelTaskById(itemBagsTasks.remove(op.getId()));
		
		trades.stream().filter(trade -> trade.containsPlayer(op)).findAny().ifPresent(trade -> trade.endTrade(false)); 
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		T op = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (itemBags.containsKey(op.getId()))
			itemBagsTasks.put(op.getId(), OlympaCore.getInstance().getTask().scheduleSyncRepeatingTask(itemBagReminderSupplier.apply(op), 1, 20 * 30));
	}
	
	
	
	
	void openMoneySelectionFor(T p, UniqueTradeManager<T> trade) {
		if (!canTradeMoney())
			return;

		addMoney(p, trade.getTradeGuiOf(p).getPlayerMoney());
		trade.selectMoney(p, 0);
		
		selectMoney.put(p.getPlayer(), new AbstractMap.SimpleEntry<UniqueTradeManager<T>, Integer>(trade, 
				OlympaCore.getInstance().getTask().runTaskLater(() -> trade.endTrade(false), 20 * 20)));
		p.getPlayer().closeInventory();
		
		Prefix.DEFAULT_GOOD.sendMessage(p.getPlayer(), "Sélectionnez l'argent à ajouter à l'échange : ");
		/*Inventory inv = Bukkit.createInventory(p.getPlayer(), InventoryType.ANVIL, Component.text("Argent à donner"));
		inv.setItem(0, ItemUtils.item(Material.DIRT, "", "§7Ecris le montant que", "§7tu souhaites donner dans", "§7le champ de texte"));
		p.getPlayer().openInventory(inv);*/
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
		return "$";
	}
	
	/**
	 * 
	 * @param p
	 * @param items
	 * @return true if bag size is > 0
	 */
	boolean setBag(T op, Collection<ItemStack> items) {
		if (items.size() == 0)
			return false;
		
		if (itemBagsTasks.containsKey(op.getId()))
			OlympaCore.getInstance().getTask().cancelTaskById(itemBagsTasks.get(op.getId()));
		
		itemBags.put(op.getId(), items.toArray(new ItemStack[items.size()]));
		itemBagsTasks.put(op.getId(), OlympaCore.getInstance().getTask().scheduleSyncRepeatingTask(itemBagReminderSupplier.apply(op), 1, 20 * 30));	
		
		return true;
	}
	
	public void flushBag(T op) {		
		if (itemBags.containsKey(op.getId()))
			if (setBag(op, op.getPlayer().getInventory().addItem(itemBags.get(op.getId())).values()))
				Prefix.DEFAULT_GOOD.sendMessage(op.getPlayer(), "Tu as récupéré certains de tes objets.");
			else
				Prefix.DEFAULT_GOOD.sendMessage(op.getPlayer(), "Tu as récupéré tous tes objets !");
	}
	
	
	public void startTrade(T p1, T p2) {
		if (itemBags.containsKey(p1.getId())) {
			Prefix.BAD.sendMessage(p1.getPlayer(), "Tu dois d'abord vider ton sac ! Fais /trade collect pour récupérer tes objets.");
			Prefix.BAD.sendMessage(p2.getPlayer(), "%s n'est pas encore prêt à démarrer un échange.", p1.getName());
			
		} else if (itemBags.containsKey(p2.getId())) {
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

	void unregister(UniqueTradeManager<T> trade) {
		if (!trades.remove(trade))
			return;
		
		trade.getPlayers().forEach(p -> {
			p.getPlayer().closeInventory();
			if (selectMoney.containsKey(p.getPlayer()))
				OlympaCore.getInstance().getTask().cancelTaskById(selectMoney.remove(p.getPlayer()).getValue());
		});
		
	}
	
	
	
	
/*
	@Override
	public boolean isEnabled() {
		return isEnabled;
	}

	@Override
	public boolean enable(OlympaCore plugin) {
		if (!isEnabled())
			return isEnabled;
		
		if (cmd == null)
			cmd = new TradeCommand(OlympaCore.getInstance(), this).register();
		
		return isEnabled = true;
	}

	@Override
	public boolean disable(OlympaCore plugin) {
		if (isEnabled())
			return isEnabled;
		
		trades.forEach(trade -> trade.endTrade(false));
		trades.clear();
		
		return isEnabled = false;
	}

	@Override
	public boolean setToPlugin(OlympaCore plugin) {
		plugin.setTradeManager(this);
		return true;
	}*/
}




