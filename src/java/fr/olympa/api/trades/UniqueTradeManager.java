package fr.olympa.api.trades;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.economy.MoneyPlayerInterface;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.trades.TradeGui.TradeStep;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class UniqueTradeManager<T extends MoneyPlayerInterface> {

	private static NamespacedKey ownerKey = NamespacedKey.fromString("is_owned_by_someone_else"); 
	
	
	private Map<T, TradeGui<T>> map = new HashMap<T, TradeGui<T>>();
	
	private int timerId;
	private TradesManager<T> manager;
	
	public UniqueTradeManager(TradesManager<T> manager, T p1, T p2) {
		map.put(p1, new TradeGui<T>(this, p1));
		map.put(p2, new TradeGui<T>(this, p2));
	}
	
	/**
	 * 
	 * @return null if money trade is disabled, otherwise money symbol
	 */
	String getMoneySymbol() {
		return manager.getMoneySymbol();
	}
	
	void click(InventoryClickEvent e) {
		e.setCancelled(true);
		
		if ((e.getClick() != ClickType.LEFT && e.getClick() != ClickType.RIGHT) || 
				e.getWhoClicked().getType() != EntityType.PLAYER)
			return;
		
		T p = AccountProvider.get(e.getWhoClicked().getUniqueId());
		TradeGui<T> pGui = map.get(p);
		
		if (e.getCurrentItem() != null) {
			if (pGui.getStep() == TradeStep.FILLING)
				if (e.getClickedInventory().getType() == InventoryType.PLAYER)
					addItemToTrade(p, e.getCurrentItem());
			
				else if (hasClickedOwnItems(e.getRawSlot()))
					removeItemFromTrade(p, e.getCurrentItem());
			
				else if (TradeGui.isMoneySelectionButton(e.getRawSlot()))
					manager.openMoneySelectionGuiFor(p, this);
		}
		
		if (e.getClickedInventory().getType() != InventoryType.PLAYER && TradeGui.isNextStepButton(e.getRawSlot()))
			if (pGui.nextTradeStep()) {
				TradeGui<T> otherGui = getOtherTrade(pGui);
				otherGui.setNextStepOther(pGui.getStep());
				if (otherGui.getStep() == pGui.getStep())
					startTradeTimer();
			}
				
	}
	
	private void addItemToTrade(T p, ItemStack item) {
		if (map.get(p).addItemOnPlayerSide(item)) {
			map.get(getOtherPlayer(p)).addItemOnOtherSide(item);
			item.setType(Material.AIR);
		}
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
		timerId = OlympaCore.getInstance().getTask().scheduleSyncRepeatingTask(() -> 
			map.forEach((p, gui) -> {
				if (!gui.updateTimer()) {
					endTrade(true);
					OlympaCore.getInstance().getTask().cancelTaskById(timerId);
					return;
				}
			})
		, 0, 20);
	}
	
	void endTrade(boolean success) {
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
		
		manager.cancelTasksFor(this);
	}
	
	
	
	private void addItems(boolean success, T p, List<ItemStack> items, double money) {
		if (success)
			Prefix.DEFAULT_GOOD.sendMessage(p.getPlayer(), "Tu as reçu " + items.size() + " objets" + (manager.canTradeMoney() ? " et " + money + manager.getMoneySymbol() : "") + " !");
		else
			Prefix.DEFAULT_BAD.sendMessage(p.getPlayer(), "L'échange a échoué, tu as récupéré tes objets" + (manager.canTradeMoney() ? " et ton argent." : "."));
		
		manager.setBag(AccountProvider.get(p.getUniqueId()), p.getPlayer().getInventory().addItem(items.toArray(new ItemStack[items.size()])).values());
		manager.addMoney(p, money);
	}
	
	
	void selectMoney(T p, double money) {
		if (manager.hasMoney(p, money))
			if (map.containsKey(p)) {
				manager.removeMoney(p, money);
				map.get(p).setPlayerMoney(money);
				map.get(getOtherPlayer(p)).setOtherMoney(money);
			}
	}
	
	
	void openFor(T p) {
		if (map.containsKey(p))
			p.getPlayer().openInventory(map.get(p).getInventory());
	}
	
	
	static ItemStack getAsLocked(ItemStack item) {
		ItemStack it = item.clone();
		it.getItemMeta().getPersistentDataContainer().set(ownerKey, PersistentDataType.BYTE, (byte) 1);
		return it;
	}
	
	
	static boolean isLocked(ItemStack item) {
		return item.getItemMeta().getPersistentDataContainer().has(ownerKey, PersistentDataType.BYTE);
	}
	

	
	public static boolean hasClickedOwnItems(int rawSlot) {
		return rawSlot > 8 && (rawSlot / 9) < 4;
	}
	
}










