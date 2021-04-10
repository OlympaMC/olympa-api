package fr.olympa.api.trades;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.trades.TradeGui.TradeStep;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public class UniqueTradeManager {

	private static NamespacedKey ownerKey = NamespacedKey.fromString("is_owned_by_someone_else"); 
	
	
	private Map<Player, TradeGui> map = new HashMap<Player, TradeGui>();
	
	private int timerId;
	
	public UniqueTradeManager(Player p1, Player p2) {
		map.put(p1, new TradeGui(this, p1));
		map.put(p2, new TradeGui(this, p2));
	}
	
	public void click(InventoryClickEvent e) {
		e.setCancelled(true);
		
		if ((e.getClick() != ClickType.LEFT && e.getClick() != ClickType.RIGHT) || 
				e.getWhoClicked().getType() != EntityType.PLAYER)
			return;
		
		Player p = (Player) e.getWhoClicked();
		TradeGui pGui = map.get(p);
		
		if (e.getCurrentItem() != null) {
			if (pGui.getStep() == TradeStep.FILLING)
				if (e.getClickedInventory().getType() == InventoryType.PLAYER)
					addItemToTrade(p, e.getCurrentItem());
			
				else if (hasClickedOwnItems(e.getRawSlot()))
					removeItemFromTrade(p, e.getCurrentItem());
			
				else if (TradeGui.isMoneySelectionButton(e.getRawSlot()))
					return;//TODO
		}
		
		if (e.getClickedInventory().getType() != InventoryType.PLAYER && TradeGui.isNextStepButton(e.getRawSlot()))
			if (pGui.nextTradeStep()) {
				TradeGui otherGui = getOtherTrade(pGui);
				otherGui.setNextStepOther(pGui.getStep());
				if (otherGui.getStep() == pGui.getStep())
					startTradeTimer();
			}
				
	}
	
	private void addItemToTrade(Player p, ItemStack item) {
		if (map.get(p).addItemOnPlayerSide(item)) {
			map.get(getOtherPlayer(p)).addItemOnOtherSide(item);
			item.setType(Material.AIR);
		}
	}
	
	private void removeItemFromTrade(Player p, ItemStack item) {
		if (map.get(p).removeItemOnPlayerSide(item))
			if (p.getInventory().addItem(item).size() == 0)
				map.get(getOtherPlayer(p)).removeItemOnOtherSide(item);
			else
				map.get(p).addItemOnPlayerSide(item);
	}
	
	
	

	public boolean containsPlayer(HumanEntity whoClicked) {
		return map.containsKey(whoClicked);
	}
	
	public Player getOtherPlayer(Player p) {
		if (map.containsKey(p))
			return map.keySet().stream().filter(pl -> !pl.getUniqueId().equals(p.getUniqueId())).findFirst().get();
		else
			return null;
	}
	
	public TradeGui getOtherTrade(TradeGui gui) {
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
	
	public void endTrade(boolean success) {
		if (success) {
			map.keySet().forEach(p -> {
				TradeGui gui = map.get(getOtherPlayer(p));
				addItems(success, p, gui.getPlayerItems(), gui.getPlayerMoney());
			});
		}else
			map.keySet().forEach(p -> {
				TradeGui gui = map.get(p);
				addItems(success, p, gui.getPlayerItems(), gui.getPlayerMoney());
			});
	}
	
	private void addItems(boolean success, Player p, List<ItemStack> items, double money) {
		if (success)
			Prefix.DEFAULT_GOOD.sendMessage(p, "Tu as reçu " + items.size() + " objets" + (TradesManager.getInstance().canTradeMoney() ? " et " + money + TradesManager.getInstance().getMoneySymbol() : "") + " !");
		else
			Prefix.DEFAULT_BAD.sendMessage(p, "L'échange a échoué, tu as récupéré tes objets" + (TradesManager.getInstance().canTradeMoney() ? " et ton argent." : "."));
		
		TradesManager.getInstance().setBag(AccountProvider.get(p.getUniqueId()), p.getInventory().addItem(items.toArray(new ItemStack[items.size()])).values());
		if (TradesManager.getInstance().canTradeMoney())
			TradesManager.getInstance().addMoney(p, money);
	}
	
	
	public void selectMoney(Player p, double money) {
		if (TradesManager.getInstance().hasMoney(p, money))
			if (map.containsKey(p)) {
				TradesManager.getInstance().removeMoney(p, money);
				map.get(p).setPlayerMoney(money);
				map.get(getOtherPlayer(p)).setOtherMoney(money);
			}
	}
	
	
	public void openFor(Player p) {
		if (map.containsKey(p))
			p.openInventory(map.get(p).getInventory());
	}
	
	
	public static ItemStack getAsLocked(ItemStack item) {
		ItemStack it = item.clone();
		it.getItemMeta().getPersistentDataContainer().set(ownerKey, PersistentDataType.BYTE, (byte) 1);
		return it;
	}
	
	
	public static boolean isLocked(ItemStack item) {
		return item.getItemMeta().getPersistentDataContainer().has(ownerKey, PersistentDataType.BYTE);
	}
	

	
	public static boolean hasClickedOwnItems(int rawSlot) {
		return rawSlot > 8 && (rawSlot / 9) < 4;
	}
	
}










