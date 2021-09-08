package fr.olympa.api.spigot.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import fr.olympa.api.utils.Prefix;

public class Inventories implements Listener {

	private static Map<Player, OlympaGUI> g = new HashMap<>();
	private static List<InventoryType> notHoldable = Arrays.asList(InventoryType.DISPENSER, InventoryType.DROPPER, InventoryType.FURNACE, InventoryType.BREWING, InventoryType.HOPPER);

	private static boolean close = false;

	public static void closeAll() {
		for (Iterator<Player> iterator = g.keySet().iterator(); iterator.hasNext();) {
			Player p = iterator.next();
			iterator.remove();
			p.closeInventory();
		}
	}

	public static void closeAndExit(Player p) {
		g.remove(p);
		p.closeInventory();
	}

	public static void closeWithoutExit(Player p) {
		if (!g.containsKey(p))
			return;
		if (p.getOpenInventory().getType() == InventoryType.CRAFTING)
			return;
		close = true;
		p.closeInventory();
	}

	public static <T extends OlympaGUI> T create(Player p, T inv) {
		//closeWithoutExit(p);
		p.closeInventory();
		p.openInventory(inv.getInventory());
		g.put(p, inv);
		return inv;
	}

	private static OlympaGUI getGUI(Inventory inv) {
		if (inv == null)
			return null;
		InventoryHolder holder = inv.getHolder();
		if (holder != null && holder instanceof OlympaGUI gui)
			return gui;
		if (notHoldable.contains(inv.getType()))
			for (OlympaGUI gui : g.values())
				if (gui.getInventory().equals(inv))
					return gui;
		return null;
	}
	
	public static boolean isInSystem(Player p) {
		return g.containsKey(p);
	}
	
	public static Map<Integer, ItemStack> getDoubleClickItems(Player p, ItemStack itemAsk, int slot, @NotNull InventoryView inventoryView) {
		int maxSize = itemAsk.getMaxStackSize();
		int maxGetSize = maxSize - itemAsk.getAmount();
		if (maxGetSize <= 0)
			return null;
		Inventory guiInventory = inventoryView.getTopInventory();
		Inventory playerInventory = inventoryView.getBottomInventory();
		int maxInvSize = guiInventory.getSize() + playerInventory.getSize();
		Map<Integer, ItemStack> itemGet = new HashMap<>();
		for (int i = 0; i < maxInvSize && maxGetSize > 0; i++) {
			@Nullable
			ItemStack item = inventoryView.getItem(i);
			if (item == null) continue;
			if (!item.isSimilar(itemAsk)) continue;
			ItemStack newItem ;
			if (item.getAmount() > maxGetSize) {
				newItem = new ItemStack(item);
				newItem.setAmount(maxGetSize);
			} else {
				newItem = item;
			}
			maxGetSize -= item.getAmount();
			itemGet.put(i, item);
		}
		return itemGet;
	}

	// TODO finish execute double click item
	public static void getDoubleClickItems(Player p, ItemStack itemAsk, Map<Integer, ItemStack> itemToSetInCusor, @NotNull InventoryView inventoryView) {
		@NotNull
		ItemStack cursor = p.getItemOnCursor();
		if (itemAsk.isSimilar(p.getItemOnCursor()))
		p.getItemOnCursor();
	}

	@EventHandler
	public synchronized void onClick(InventoryClickEvent event) {
		if (!(event.getWhoClicked() instanceof Player)) {
			event.setCancelled(true);
			return;
		}
		Player p = (Player) event.getWhoClicked();
		@Nullable
		Inventory inv = event.getClickedInventory();
		@Nullable
		ItemStack current = event.getCurrentItem();
		@Nullable
		ItemStack cursor = event.getCursor();
//		System.out.print(String.format("Debug Info Gui Error : player > %s current > %s cursor > %s click > %s action > %s isFromInv > %s", p.getName(),
//				current != null ? current.getType().name() : "null",current != null ? current.getType().name() : "null",  event.getClick().name(), event.getAction().name(), inv == p.getInventory()));
		try {
			OlympaGUI gui = getGUI(event.getView().getTopInventory());
			if (gui == null)
				return;
			if (event.getClick() == ClickType.DOUBLE_CLICK) {
				if (gui.noDoubleClick()) {
					event.setCancelled(true);
					return;
				}
//				if (cursor == null) {
//					getDoubleClickItems(p, current, event.getSlot(), event.getView());
//				}
			}
			boolean isShiftClic = event.isShiftClick() && current != null && current.getType() != Material.AIR;
			if (inv == p.getInventory()) {
				if (isShiftClic)
					event.setCancelled(gui.onMoveItem(p, current, true, event.getSlot()));
				return;
			}
			if (isShiftClic && gui.onMoveItem(p, current, false, event.getSlot())) {
				event.setCancelled(true);
				return;
			}
			switch (event.getClick()) {
			case LEFT:
				if (gui.noLeftClick()) {
					event.setCancelled(true);
					return;
				}
				break;
			case MIDDLE:
				if (gui.noMiddleClick()) {
					event.setCancelled(true);
					return;
				}
				break;
			case DROP:
				if (gui.noDropClick()) {
					event.setCancelled(true);
					return;
				}
				break;
			case NUMBER_KEY:
				if (gui.noNumberKey()) {
					event.setCancelled(true);
					return;
				}
				break;
			case RIGHT:
				if (gui.noRightClick()) {
					event.setCancelled(true);
					return;
				}
				break;
			default:
				break;
			}
			if (cursor == null || cursor.getType() == Material.AIR) {
				if (current == null || current.getType() == Material.AIR) {
					event.setCancelled(true);
					return;
				}
				event.setCancelled(gui.onClick(p, current, event.getSlot(), event.getClick()));
			} else
				event.setCancelled(gui.onClickCursor(p, current, cursor, event.getSlot()));
		} catch (Exception | NoClassDefFoundError e) {
			event.setCancelled(true);
			closeAndExit(p);
			p.sendMessage(Prefix.ERROR.formatMessage("Une erreur est survenue : &4%s&c.", e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
			System.err.print(String.format("Debug Info Gui Error : player > %s current > %s cursor > %s click > %s action > %s isFromInv > %s\n", p.getName(),
					current != null ? current.getType().name() : "null",current != null ? current.getType().name() : "null",  event.getClick().name(), event.getAction().name(), inv == p.getInventory()));
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		OlympaGUI gui = getGUI(e.getView().getTopInventory());
		if (gui == null)
			return;
		e.setCancelled(gui.noDragClick());
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e) {
		Player p = (Player) e.getPlayer();
		if (close) {
			close = false;
			return;
		}
		if (!g.containsKey(p))
			return;
		OlympaGUI gui = getGUI(e.getView().getTopInventory());
		if (gui == null)
			return;
		if (gui.onClose(p))
			g.remove(p);
	}

}