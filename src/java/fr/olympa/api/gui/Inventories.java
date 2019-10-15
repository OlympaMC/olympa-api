package fr.olympa.api.gui;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class Inventories implements Listener{ // TODO listener à enregistrer au moment du lancement du Core

	private static Map<Player, Map.Entry<CustomInventory, Inventory>> g = new HashMap<>();

	private static boolean close = false;

	public static Inventory createGetInv(Player p, CustomInventory inv){
		put(p, inv, inv.open(p));
		return g.get(p).getValue();
	}
	
	/**
	 * Open a CustomInventory to player, and insert it to the Inventories system.
	 * @param p Player to open
	 * @param inv CustomInventory instance to open
	 * @param <T> Class who implements the CustomInventory interface
	 * @return Same CustomInventory
	 */
	public static <T extends CustomInventory> T create(Player p, T inv) {
		closeWithoutExit(p);
		Inventory tinv = inv.open(p);
		if (tinv == null) return inv;
		put(p, inv, tinv);
		return inv;
	}
	
	@EventHandler
	public void onClick(InventoryClickEvent e){
		Player p = (Player) e.getWhoClicked();
		Inventory inv = e.getClickedInventory();
		ItemStack current = e.getCurrentItem();

		if (g.get(p) == null) return;
		if (inv == null) return;
		
		e.setCancelled(false);
		
		if (inv == p.getInventory()){
			if (e.isShiftClick()) e.setCancelled(true);
			return;
		}

		if (!inv.equals(g.get(p).getValue())) return;
		
		if (e.getCursor().getType() == Material.AIR) {
			if (current == null || current.getType() == Material.AIR) return;
			if (g.get(p).getKey().onClick(p, inv, current, e.getSlot(), e.getClick())) e.setCancelled(true);
		}else {
			if (g.get(p).getKey().onClickCursor(p, inv, current, e.getCursor(), e.getSlot()))
				e.setCancelled(true);
		}
	}

	@EventHandler
	public void onClose(InventoryCloseEvent e){
		Player p = (Player) e.getPlayer();
		if (close){
			close = false;
			return;
		}
		if (g.containsKey(p)) {
			Entry<CustomInventory, Inventory> inv = g.get(p);
			if (!e.getInventory().equals(inv.getValue())) return;
			if (inv.getKey().onClose(p, inv.getValue())) g.remove(p);
		}
	}
	
	
	public static void closeWithoutExit(Player p){
		if (!g.containsKey(p)) return;
		if (p.getOpenInventory().getType() == InventoryType.CRAFTING){
			return;
		}
		close = true;
		p.closeInventory();
	}
	
	public static void closeAndExit(Player p){
		g.remove(p);
		p.closeInventory();
	}
	
	public static void closeAll(){
		for (Iterator<Player> iterator = g.keySet().iterator(); iterator.hasNext();) {
			Player p = (Player) iterator.next();
			iterator.remove();
			p.closeInventory();
		}
	}
	
	public static void put(Player p, CustomInventory cinv, Inventory inv){
		g.put(p, new AbstractMap.SimpleEntry<CustomInventory, Inventory>(cinv, inv));
	}
	
	public static boolean isInSystem(Player p){
		return g.containsKey(p);
	}
	
	public static void openInventory(Player p){
		p.openInventory(g.get(p).getValue());
	}

}