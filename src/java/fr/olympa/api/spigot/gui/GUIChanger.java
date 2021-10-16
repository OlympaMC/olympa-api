package fr.olympa.api.spigot.gui;

import org.bukkit.inventory.ItemStack;

public interface GUIChanger {
	
	public void setItem(int slot, ItemStack item);
	
	public ItemStack getItem(int slot);
	
	public void clear();
	
	public int getRows();
	
	public int getColumns();
	
	public default int getSize() {
		return getRows() * getColumns();
	}
	
	abstract class GUIChangerChild implements GUIChanger {
		
		private GUIView view;
		private int rows;
		private int columns;
		
		protected GUIChangerChild(GUIView view, int rows, int columns) {
			this.view = view;
			this.rows = rows;
			this.columns = columns;
		}
		
		public abstract int getInvSlot(int slot);
		
		@Override
		public void setItem(int slot, ItemStack item) {
			view.setItem(getInvSlot(slot), item);
		}
		
		@Override
		public ItemStack getItem(int slot) {
			return view.getItem(getInvSlot(slot));
		}
		
		@Override
		public void clear() {
			for (int i = 0; i < rows * columns; i++) view.setItem(i, null);
		}
		
		@Override
		public int getRows() {
			return rows;
		}
		
		@Override
		public int getColumns() {
			return columns;
		}
		
	}
	
}
