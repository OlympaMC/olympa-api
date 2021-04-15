package fr.olympa.api.trades;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.trades.TradeGui.TradeStep;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class UniqueTradeManager<T extends TradePlayerInterface> {

	private static NamespacedKey ownerKey = NamespacedKey.fromString("is_owned_by_someone_else"); 
	
	
	private Map<T, TradeGui<T>> map = new HashMap<T, TradeGui<T>>();
	
	private int timerId;
	private TradesManager<T> manager;
	
	private boolean hasEnded = false;
	
	UniqueTradeManager(TradesManager<T> manager, T p1, T p2) {
		this.manager = manager;
		
		map.put(p1, new TradeGui<T>(this, p1, p2));
		map.put(p2, new TradeGui<T>(this, p2, p1));
		
		map.forEach((p, gui) -> p.getPlayer().openInventory(gui.getInventory()));
	}
	
	/**
	 * 
	 * @return null if money trade is disabled, otherwise money symbol
	 */
	String getMoneySymbol() {
		return manager.getMoneySymbol();
	}
	
	void click(InventoryClickEvent e, T p) {
		e.setCancelled(true);
		
		if ((e.getClick() != ClickType.LEFT && e.getClick() != ClickType.RIGHT) || 
				e.getWhoClicked().getType() != EntityType.PLAYER || e.getCurrentItem() == null)
			return;
		
		TradeGui<T> pGui = map.get(p);
		
		if (pGui.getStep() == TradeStep.FILLING)
			if (e.getClickedInventory().getType() == InventoryType.PLAYER) {
				addItemToTrade(p, e.getCurrentItem());
				e.setCurrentItem(null);
				
			}else if (!isLocked(e.getCurrentItem()))
				removeItemFromTrade(p, e.getCurrentItem());
		
			else if (TradeGui.isMoneySelectionButton(e.getRawSlot()))
				manager.openMoneySelectionFor(p, this);
			
		
		if (pGui.getInventory().equals(e.getClickedInventory()) && TradeGui.isNextStepButton(e.getRawSlot()))
			if (pGui.nextTradeStep()) {
				TradeGui<T> otherGui = getOtherTrade(pGui);
				otherGui.setNextStepOther(pGui.getStep());
				if (otherGui.getStep() == TradeStep.TIMER && pGui.getStep() == TradeStep.TIMER)
					startTradeTimer();
			}
				
	}
	
	private void addItemToTrade(T p, ItemStack item) {
		if (map.get(p).addItemOnPlayerSide(item)) 
			map.get(getOtherPlayer(p)).addItemOnOtherSide(item);
	}
	
	private void removeItemFromTrade(T p, ItemStack item) {
		if (map.get(p).removeItemOnPlayerSide(item))
			if (p.getPlayer().getInventory().addItem(item).size() == 0)
				map.get(getOtherPlayer(p)).removeItemOnOtherSide(item);
			else
				map.get(p).addItemOnPlayerSide(item);
	}
	
	
	


	public Set<T> getPlayers() {
		return map.keySet();
	}
	
	public boolean containsPlayer(T p) {
		return map.containsKey(p);
	}
	
	public T getOtherPlayer(T p) {
		if (map.containsKey(p))
			return map.keySet().stream().filter(pl -> !pl.equals(p)).findFirst().get();
		else
			return null;
	}
	
	
	TradeGui<T> getTradeGuiOf(T p) {
		return map.get(p);
	}
	
	TradeGui<T> getOtherTrade(TradeGui<T> gui) {
		if (map.containsValue(gui))
			return map.values().stream().filter(gui2 -> !gui2.equals(gui)).findFirst().get();
		else
			return null;
	}
	
	
	
	private void startTradeTimer() {		
		timerId = OlympaCore.getInstance().getTask().scheduleSyncRepeatingTask(() -> {
			
			for (Entry<T, TradeGui<T>> e : map.entrySet())
				if (!e.getValue().updateTimer()) {
					endTrade(true);
					OlympaCore.getInstance().getTask().cancelTaskById(timerId);
					return;
				}
		
		
		}, 0, 20);
	}
	
	void endTrade(boolean success) {
		if (hasEnded)
			return;
		
		hasEnded = true;
		map.keySet().forEach(p -> p.getPlayer().closeInventory());
		
		if (success) {
			map.keySet().forEach(p -> {
				TradeGui<T> gui = map.get(getOtherPlayer(p));
				addItems(success, p, gui.getPlayerItems(), gui.getPlayerMoney());
			});
		}else
			map.keySet().forEach(p -> {
				TradeGui<T> gui = map.get(p);
				addItems(success, p, gui.getPlayerItems(), gui.getPlayerMoney());
			});
		
		manager.unregister(this);
	}
	
	
	
	private void addItems(boolean success, T p, Collection<ItemStack> items, double money) {
		int count = items.stream().mapToInt(it -> it.getAmount()).sum();
		if (success)
			Prefix.DEFAULT_GOOD.sendMessage(p.getPlayer(), "Tu as reçu " + count + (count <= 1 ? " objet" : " objets") + (manager.canTradeMoney() ? " et " + money + manager.getMoneySymbol() : "") + " !");
		else
			Prefix.DEFAULT_BAD.sendMessage(p.getPlayer(), "L'échange a échoué, tu as récupéré tes objets" + (manager.canTradeMoney() ? " et ton argent." : "."));
		
		p.getTradeBag().setItems(items);
		//manager.setBag(AccountProvider.get(p.getUniqueId()), p.getPlayer().getInventory().addItem(items.toArray(new ItemStack[items.size()])).values());
		manager.addMoney(p, money);
	}
	
	
	void selectMoney(T p, double money) {
		if (hasEnded)
			return;
		
		if (manager.hasMoney(p, money))
			if (map.containsKey(p)) {
				manager.removeMoney(p, money);
				map.get(p).setPlayerMoney(money);
				map.get(getOtherPlayer(p)).setOtherMoney(money);
			}
	}
	
	
	void openFor(T p) {
		if (hasEnded)
			return;
		
		if (map.containsKey(p))
			p.getPlayer().openInventory(map.get(p).getInventory());
	}
	
	
	static ItemStack getAsLocked(ItemStack item) {
		ItemStack it = item.clone();
		ItemMeta meta = it.getItemMeta();
		meta.getPersistentDataContainer().set(ownerKey, PersistentDataType.BYTE, (byte) 1);
		it.setItemMeta(meta);
		return it;
	}
	
	
	static boolean isLocked(ItemStack item) {
		return item.getItemMeta().getPersistentDataContainer().has(ownerKey, PersistentDataType.BYTE);
	}	
}










