package fr.olympa.api.spigot.gui.templates;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.GUIView;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Hierarchy;

public abstract class HierarchyView<T> extends SplitView {
	
	//private static final int ROWS = 5;
	private static final int BAR_RIGHT_OFFSET = 2;
	
	private static final ItemStack BACK_ITEM = ItemUtils.skullCustom("§e← Revenir au précédent", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODY1MmUyYjkzNmNhODAyNmJkMjg2NTFkN2M5ZjI4MTlkMmU5MjM2OTc3MzRkMThkZmRiMTM1NTBmOGZkYWQ1ZiJ9fX0=");
	
	private final String observeCode = "hierarchyGUI-" + hashCode();
	private Hierarchy<T> hierarchy;
	private Hierarchy<T> tmpHierarchy;
	
	private Map<Integer, Hierarchy<T>> hierarchies = new HashMap<>();
	
	private int page = 0;
	private int maxPage;
	
	protected HierarchyView(DyeColor barColor, Hierarchy<T> hierarchy) {
		super(barColor, BAR_RIGHT_OFFSET);
		tmpHierarchy = hierarchy;
	}
	
	@Override
	public void init() {
		super.init();
		setViews(new Left(), new Right());
		
		setHierarchy(tmpHierarchy);
		tmpHierarchy = null;
	}
	
	public void setHierarchy(Hierarchy<T> hierarchy) {
		if (hierarchy == null) throw new IllegalArgumentException("Hierarchy cannot be null");
		if (this.hierarchy != hierarchy) {
			if (this.hierarchy != null) this.hierarchy.unobserve(observeCode);
			this.hierarchy = hierarchy;
			hierarchy.observe(observeCode, () -> setHierarchy(hierarchy));
		}
		calculateMaxPage();
		hierarchies.clear();
		
		right.setItem(2, hierarchy.hasParent() ? BACK_ITEM : null);
		setItems();
	}
	
	private void setItems() {
		left.clear();
		
		int mainSlot = page * leftItems;
		int maxSlot = mainSlot + leftItems;
		for (Hierarchy<T> sub : hierarchy.getSubHierarchies().values()) {
			if (mainSlot == maxSlot) return;
			hierarchies.put(mainSlot, sub);
			left.setItem(mainSlot++, getSubHierarchyItem(sub));
		}
		for (T object : hierarchy.getObjects()) {
			if (mainSlot == maxSlot) return;
			left.setItem(mainSlot++, getObjectItem(object));
		}
	}
	
	private void calculateMaxPage() {
		int allSize = hierarchy.getObjects().size() + hierarchy.getSubHierarchies().size();
		this.maxPage = allSize == 0 ? 1 : (int) Math.ceil(allSize * 1D / leftItems);
		if (page >= maxPage) page = maxPage - 1;
	}
	
	@Override
	public boolean onClose(Player p) {
		hierarchy.unobserve(observeCode);
		return super.onClose(p);
	}
	
	/**
	 * Called when an object is clicked
	 * @param existing clicked object
	 * @param p player which clicks
	 * @param click click type
	 */
	public abstract void click(T existing, Player p, ClickType click);
	
	protected ItemStack getSubHierarchyItem(Hierarchy<T> subHierarchy) {
		return ItemUtils.item(Material.CHEST, "§7" + subHierarchy.getPath(), "§8> §7" + subHierarchy.getSubHierarchies().size() + " dossiers", "§8> §7" + subHierarchy.getObjects().size() + " objets");
	}
	
	protected abstract ItemStack getObjectItem(T object);
	
	private class Left extends GUIView {
		
		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			int totalSlot = slot + page * leftItems;
			int objectSlot = totalSlot - hierarchy.getSubHierarchies().size();
			if (objectSlot < 0) {
				setHierarchy(hierarchies.get(totalSlot));
			}else {
				click(hierarchy.getObjects().get(objectSlot), p, click);
			}
			return true;
		}
		
	}
	
	private class Right extends GUIView {
		
		@Override
		public void init() {
			setItem(0, PagedView.previousPage);
			setItem(getRows() - 1, PagedView.nextPage);
		}
		
		@Override
		public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
			if (slot == 0) {
				if (page != 0) {
					page--;
					setItems();
				}
			}else if (slot == getRows() - 1) {
				if (page + 1 != maxPage) {
					page++;
					setItems();
				}
			}else if (slot == 2) {
				setHierarchy(hierarchy.getParent());
			}
			return true;
		}
		
	}
	
}
