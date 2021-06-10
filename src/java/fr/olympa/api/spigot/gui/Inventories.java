package fr.olympa.api.spigot.gui;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

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
		if (holder != null && holder instanceof OlympaGUI) // TODO CHECK
			return (OlympaGUI) holder;
		if (notHoldable.contains(inv.getType()))
			for (OlympaGUI gui : g.values())
				if (gui.getInventory().equals(inv))
					return gui;
		return null;
	}

	public static boolean isInSystem(Player p) {
		return g.containsKey(p);
	}

	@EventHandler
	public synchronized void onClick(InventoryClickEvent event) {
		Player p = (Player) event.getWhoClicked();
		try {
			Inventory inv = event.getClickedInventory();
			ItemStack current = event.getCurrentItem();

			OlympaGUI gui = getGUI(event.getView().getTopInventory());
			if (gui == null)
				return;

			event.setCancelled(false);

			if (inv == p.getInventory()) {
				if (event.isShiftClick() && current != null && current.getType() != Material.AIR)
					event.setCancelled(gui.onMoveItem(p, current));
				return;
			}
			if (gui.noDoubleClick() && event.getClick() == ClickType.DOUBLE_CLICK)
				return;

			if (event.getCursor().getType() == Material.AIR) {
				if (current == null || current.getType() == Material.AIR)
					return;
				event.setCancelled(gui.onClick(p, current, event.getSlot(), event.getClick()));
			} else
				event.setCancelled(gui.onClickCursor(p, current, event.getCursor(), event.getSlot()));
		} catch (Exception | NoClassDefFoundError e) {
			event.setCancelled(true);
			closeAndExit(p);
			p.sendMessage(Prefix.ERROR.formatMessage("Une erreur est survenue : &4%s&c.", e.getMessage() == null ? e.getClass().getName() : e.getMessage()));
			e.printStackTrace();
		}
	}

	@EventHandler
	public void onDrag(InventoryDragEvent e) {
		OlympaGUI gui = getGUI(e.getView().getTopInventory());
		if (gui == null)
			return;
		e.setCancelled(true);
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