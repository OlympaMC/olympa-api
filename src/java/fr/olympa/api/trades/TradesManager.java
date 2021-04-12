package fr.olympa.api.trades;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class TradesManager implements Listener {

	private static TradesManager tradesManager;
	private static TradesManager getInstance() {
		return tradesManager;
	}

	private Function<OlympaPlayer, Runnable> itemBagReminderSupplier = p -> 
		() -> Prefix.DEFAULT_BAD.sendMessage(p.getPlayer(), "Tu n'avais pas assez de place dans ton inventaire, certains objets n'ont pas pu y être placés. Fais /A DEFINIR pour les récupérer.\n§4Tu as jusqu'à ce soir, demain ils seront perdus !");
	
	private Map<Long, ItemStack[]> itemBags = new HashMap<Long, ItemStack[]>();
	private Map<Long, Integer> itemBagsTasks = new HashMap<Long, Integer>();

	private Set<UniqueTradeManager> trades = new HashSet<UniqueTradeManager>();

	private Map<Player, UniqueTradeManager> selectMoney = new HashMap<Player, UniqueTradeManager>();
	private Map<Player, Integer> selectMoneyTask = new HashMap<Player, Integer>();
	
	private BiFunction<Player, Double, Boolean> hasMoney;
	private BiConsumer<Player, Double> addMoney;
	private BiConsumer<Player, Double> removeMoney;
	private String moneySymbol;
	
	public TradesManager(JavaPlugin plugin, 
			BiFunction<Player, Double, Boolean> hasMoney, 
			BiConsumer<Player, Double> addMoney,  
			BiConsumer<Player, Double> removeMoney,
			String moneySymbol) {
		
		tradesManager = this;
		
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		
		this.hasMoney = hasMoney;
		this.addMoney = addMoney;
		this.removeMoney = removeMoney;
		this.moneySymbol = moneySymbol;
	}
	
	public TradesManager(JavaPlugin plugin) {
		this(plugin, null, null, null, null);
	}
	
	
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e) {
		if (e.getClickedInventory() == null || e.getWhoClicked() instanceof Player)
			return;
		
		if (selectMoney.containsKey(e.getWhoClicked()) && e.getInventory().getType() == InventoryType.ANVIL) {
			
			if (e.getRawSlot() == 2) {
				try {
					selectMoney.get(e.getWhoClicked()).selectMoney((Player) e.getWhoClicked(), Double.valueOf(((AnvilInventory)e.getInventory()).getResult().getItemMeta().getDisplayName()));
				}catch(NumberFormatException ex) {
					selectMoney.get(e.getWhoClicked()).selectMoney((Player) e.getWhoClicked(), 0);
				}
				
				((Player)e.getWhoClicked()).closeInventory();
				OlympaCore.getInstance().getTask().cancelTaskById(selectMoneyTask.remove(e.getWhoClicked()));
			}

			e.setCancelled(true);
			return;
		}
		
		trades.stream().filter(trade -> trade.containsPlayer(e.getWhoClicked())).findAny().ifPresent(trade -> trade.click(e));
	}
	 
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		if (e.getPlayer().getType() != EntityType.PLAYER)
			return;
		
		if (selectMoney.containsKey(e.getPlayer())) {
			if (e.getInventory().getType() != InventoryType.ANVIL)
				return;
			
			selectMoney.remove(e.getPlayer()).openFor((Player) e.getPlayer());
			OlympaCore.getInstance().getTask().cancelTaskById(selectMoneyTask.remove(e.getPlayer()));
			return;	
		}
		
		trades.stream().filter(trade -> trade.containsPlayer(e.getPlayer())).findAny().ifPresent(trade -> trade.endTrade(false));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		OlympaPlayer op = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (itemBagsTasks.containsKey(op.getId()))
			OlympaCore.getInstance().getTask().cancelTaskById(itemBagsTasks.remove(op.getId()));
		
		trades.stream().filter(trade -> trade.containsPlayer(e.getPlayer())).findAny().ifPresent(trade -> trade.endTrade(false)); 
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		OlympaPlayer op = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (itemBags.containsKey(op.getId()))
			itemBagsTasks.put(op.getId(), OlympaCore.getInstance().getTask().scheduleSyncRepeatingTask(itemBagReminderSupplier.apply(op), 1, 20 * 30));
	}
	
	
	
	
	@SuppressWarnings("deprecation")
	void openMoneySelectionGuiFor(Player p, UniqueTradeManager trade) {
		if (!canTradeMoney())
			return;
		
		selectMoney.put(p, trade);
		selectMoneyTask.put(p, OlympaCore.getInstance().getTask().runTaskLater(() -> trade.endTrade(false), 20 * 20));
		
		AnvilInventory inv = (AnvilInventory) Bukkit.createInventory(p, InventoryType.ANVIL, "Argent à donner");
		p.openInventory(inv);
		
		inv.setFirstItem(ItemUtils.item(Material.DIRT, "", "§7Ecris le montant que", "§7tu souhaites donner dans", "§7le champ de texte"));
		p.updateInventory();
	}
	
	
	
	
	
	
	public boolean canTradeMoney() {
		return hasMoney != null;
	}
	
	boolean hasMoney(Player p, double money) {
		return canTradeMoney() ? hasMoney.apply(p, money) : false;
	}
	
	void addMoney(Player p, double amount) {
		if (canTradeMoney())
			addMoney.accept(p, amount);
	}
	
	void removeMoney(Player p, double amount) {
		if (canTradeMoney())
			removeMoney.accept(p, amount);
	}
	
	String getMoneySymbol() {
		return moneySymbol;
	}
	
	/**
	 * 
	 * @param p
	 * @param items
	 * @return true if bag size is > 0
	 */
	boolean setBag(OlympaPlayer op, Collection<ItemStack> items) {
		if (items.size() == 0)
			return false;
		
		if (itemBagsTasks.containsKey(op.getId()))
			OlympaCore.getInstance().getTask().cancelTaskById(itemBagsTasks.get(op.getId()));
		
		itemBags.put(op.getId(), items.toArray(new ItemStack[items.size()]));
		itemBagsTasks.put(op.getId(), OlympaCore.getInstance().getTask().scheduleSyncRepeatingTask(itemBagReminderSupplier.apply(op), 1, 20 * 30));	
		
		return true;
	}
	
	public void flushBag(OlympaPlayer op) {		
		if (itemBags.containsKey(op.getId()))
			if (setBag(op, op.getPlayer().getInventory().addItem(itemBags.get(op.getId())).values()))
				Prefix.DEFAULT_GOOD.sendMessage(op.getPlayer(), "Tu as récupéré certains de tes objets.");
			else
				Prefix.DEFAULT_GOOD.sendMessage(op.getPlayer(), "Tu as récupéré tous tes objets !");
	}
}




