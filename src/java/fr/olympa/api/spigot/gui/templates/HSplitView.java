package fr.olympa.api.spigot.gui.templates;

import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.GUIChanger;
import fr.olympa.api.spigot.gui.GUIView;
import fr.olympa.api.spigot.item.ItemUtils;

public class HSplitView extends GUIView {
	
	protected final int barRightOffset;
	
	protected DyeColor barColor;
	protected int barColumn;
	
	protected GUIView left;
	protected int leftItems;
	protected GUIView right;
	protected int rightItems;
	
//	protected HSplitView(int barRightWidth) {
//		
//	}
	
	protected HSplitView(DyeColor barColor, int barRightOffset) {
		this.barColor = barColor;
		this.barRightOffset = barRightOffset;
	}
	
	@Override
	public void init() {
		super.init();
		barColumn = getColumns() - barRightOffset;
		setSeparatorItems(barColor);
	}
	
	protected void setViews(GUIView left, GUIView right) {
		this.left = left;
		this.right = right;
		
		left.setGUI(new GUIChanger.GUIChangerChild(this, getRows(), barColumn) {
			@Override
			public int getInvSlot(int slot) {
				int line = (int) Math.floor(slot * 1.0 / barColumn);
				return slot + ((9 - barColumn) * line);
			}
		});
		leftItems = left.getSize();
		
		right.setGUI(new GUIChanger.GUIChangerChild(this, getRows(), barRightOffset - 1) {
			@Override
			public int getInvSlot(int slot) {
				int line = (int) Math.floor(slot * 1.0 / (8 - barColumn));
				return line * 9 + barColumn + slot + 1 - line * (8 - barColumn);
			}
		});
		rightItems = right.getSize();
	}
	
	protected void setSeparatorItems(DyeColor color) {
		this.barColor = color;
		for (int i = 0; i < getRows(); i++) setItem(i * 9 + barColumn, ItemUtils.itemSeparator(color));
	}
	
	/*private void clearLeft() {
		int row = 0;
		int column = 0;
		for (int mainSlot = 0; mainSlot < itemsInMain; mainSlot++) {
			setItem(column + row * 9, null);
			if (++column == barColumn) {
				row++;
				column = 0;
			}
		}
	}*/
	
	@Override
	public final boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		int column = slot % 9;
		if (column < barColumn) {
			int line = (int) Math.floor(slot * 1D / 9D);
			int leftSlot = slot - line * (9 - barColumn);
			return left.onClick(p, current, leftSlot, click);
		}else if (column > barColumn) {
			int rightSlot = (int) (slot - Math.floor(slot * 1D / (barColumn + 1D)) * (barColumn + 1));
			return right.onClick(p, current, rightSlot, click);
		}else return true; // split column
	}
	
	@Override
	public boolean onClose(Player p) {
		boolean close = left.onClose(p);
		if (right.onClose(p)) {
			return close;
		}
		return false;
	}
	
}
