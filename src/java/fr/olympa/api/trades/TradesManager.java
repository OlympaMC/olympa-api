package fr.olympa.api.trades;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.item.ItemUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

/**
 * Instantiate this class to enable trades. Command /trade is registered internally
 *
 * @param <T> OlympaPlayer implementation for current plugin
 */
public class TradesManager<T extends MoneyPlayerInterface> implements Listener {

	private boolean isEnabled = false;
	//private OlympaCommand cmd = null;
	
	private Function<T, Runnable> itemBagReminderSupplier = p -> 
		() -> Prefix.DEFAULT_BAD.sendMessage(p.getPlayer(), "Tu n'avais pas assez de place dans ton inventaire, certains objets n'ont pas pu y être placés. Fais /A DEFINIR pour les récupérer.\n§4Tu as jusqu'à ce soir, demain ils seront perdus !");
	
	private Map<Long, ItemStack[]> itemBags = new HashMap<Long, ItemStack[]>();
	private Map<Long, Integer> itemBagsTasks = new HashMap<Long, Integer>();

	private Set<UniqueTradeManager<T>> trades = new HashSet<UniqueTradeManager<T>>();

	private Map<Player, UniqueTradeManager<T>> selectMoney = new HashMap<Player, UniqueTradeManager<T>>();
	private Map<Player, Integer> selectMoneyTask = new HashMap<Player, Integer>();
	
	/*
	private BiFunction<T, Double, Boolean> hasMoney;
	private BiConsumer<T, Double> addMoney;
	private BiConsumer<T, Double> removeMoney;
	private String moneySymbol;*/
	
	private boolean canTradeMoney = true;
	
	public TradesManager(OlympaAPIPlugin plugin) {
		this(plugin, true);
		
		/*
		this.hasMoney = hasMoney;
		this.addMoney = addMoney;
		this.removeMoney = removeMoney;
		this.moneySymbol = moneySymbol;*/
	}
	
	public TradesManager(OlympaAPIPlugin plugin, boolean canTradeMoney) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		new TradeCommand(plugin, this).register();
		
		this.canTradeMoney = canTradeMoney;
	}
	
	
	
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
	public void onInvClick(InventoryClickEvent e) {
		if (e.getClickedInventory() == null || e.getWhoClicked() instanceof Player)
			return;
		
		if (selectMoney.containsKey(e.getWhoClicked())) {
			
			if (e.getRawSlot() == 2 && trades.contains(selectMoney.get(e.getWhoClicked()))) {
				try {
					selectMoney.get(e.getWhoClicked()).selectMoney(AccountProvider.get(e.getWhoClicked().getUniqueId()), Double.valueOf(((AnvilInventory)e.getInventory()).getResult().getItemMeta().getDisplayName()));
				}catch(NumberFormatException ex) {
					selectMoney.get(e.getWhoClicked()).selectMoney(AccountProvider.get(e.getWhoClicked().getUniqueId()), 0);
				}
				
				OlympaCore.getInstance().getTask().cancelTaskById(selectMoneyTask.remove(e.getWhoClicked()));
				((Player)e.getWhoClicked()).closeInventory();
			}

			e.setCancelled(true);
			return;
		}
		
		trades.stream().filter(trade -> trade.containsPlayer(AccountProvider.get(e.getWhoClicked().getUniqueId()))).findAny().ifPresent(trade -> trade.click(e));
	}
	 
	@EventHandler
	public void onInvClose(InventoryCloseEvent e) {
		if (e.getPlayer().getType() != EntityType.PLAYER)
			return;
		
		if (selectMoney.containsKey(e.getPlayer())) {
			//si le joueur ferme l'inventaire de l'échange car il vient d'ouvrir celui de la sélection de la money tout va bien
			if (e.getInventory().equals(selectMoney.get(e.getPlayer()).getTradeGuiOf(AccountProvider.get(e.getPlayer().getUniqueId())).getInventory()))
				return;
			
			selectMoney.remove(e.getPlayer()).openFor((AccountProvider.get(e.getPlayer().getUniqueId())));
			OlympaCore.getInstance().getTask().cancelTaskById(selectMoneyTask.remove(e.getPlayer()));
			return;	
		}
		
		trades.stream().filter(trade -> trade.containsPlayer(AccountProvider.get(e.getPlayer().getUniqueId()))).findAny().ifPresent(trade -> trade.endTrade(false));
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		OlympaPlayer op = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (itemBagsTasks.containsKey(op.getId()))
			OlympaCore.getInstance().getTask().cancelTaskById(itemBagsTasks.remove(op.getId()));
		
		trades.stream().filter(trade -> trade.containsPlayer(AccountProvider.get(e.getPlayer().getUniqueId()))).findAny().ifPresent(trade -> trade.endTrade(false)); 
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		T op = AccountProvider.get(e.getPlayer().getUniqueId());
		
		if (itemBags.containsKey(op.getId()))
			itemBagsTasks.put(op.getId(), OlympaCore.getInstance().getTask().scheduleSyncRepeatingTask(itemBagReminderSupplier.apply(op), 1, 20 * 30));
	}
	
	
	
	
	@SuppressWarnings("deprecation")
	void openMoneySelectionGuiFor(T p, UniqueTradeManager<T> trade) {
		if (!canTradeMoney())
			return;
		
		selectMoney.put(p.getPlayer(), trade);
		selectMoneyTask.put(p.getPlayer(), OlympaCore.getInstance().getTask().runTaskLater(() -> trade.endTrade(false), 20 * 20));
		
		AnvilInventory inv = (AnvilInventory) Bukkit.createInventory(p.getPlayer(), InventoryType.ANVIL, "Argent à donner");
		inv.setFirstItem(ItemUtils.item(Material.DIRT, "", "§7Ecris le montant que", "§7tu souhaites donner dans", "§7le champ de texte"));
		p.getPlayer().openInventory(inv);
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
		if (!isEnabled)
			return;
		
		if (itemBags.containsKey(p1.getId())) {
			Prefix.BAD.sendMessage(p1.getPlayer(), "Tu dois d'abord vider ton sac ! Fais /trade collect pour récupérer tes objets.");
			Prefix.BAD.sendMessage(p2.getPlayer(), "%s n'est pas encore prêt à démarrer un échange.", p1.getName());
			
		} else if (itemBags.containsKey(p2.getId())) {
			Prefix.BAD.sendMessage(p2.getPlayer(), "Tu dois d'abord vider ton sac ! Fais /trade collect pour récupérer tes objets.");
			Prefix.BAD.sendMessage(p1.getPlayer(), "%s n'est pas encore prêt à démarrer un échange.", p2.getName());
			
		}else if (trades.stream().anyMatch(trade -> trade.containsPlayer(p1))) {
			Prefix.BAD.sendMessage(p2.getPlayer(), "%s est déjà en échange, réessaies dans quelques minutes.", p1.getName());
			
		}else if (trades.stream().anyMatch(trade -> trade.containsPlayer(p2))) {
			Prefix.BAD.sendMessage(p1.getPlayer(), "%s est déjà en échange, réessaies dans quelques minutes.", p2.getName());
			
		}else
			trades.add(new UniqueTradeManager<T>(this, p1, p2));
	}

	void cancelTasksFor(UniqueTradeManager<T> trade) {
		if (!trades.remove(trade))
			return;
		
		trade.getPlayers().forEach(p -> {
			p.getPlayer().closeInventory();
			if (trade.equals(selectMoney.remove(p.getPlayer())))
				OlympaCore.getInstance().getTask().cancelTaskById(selectMoneyTask.remove(p.getPlayer()));
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




