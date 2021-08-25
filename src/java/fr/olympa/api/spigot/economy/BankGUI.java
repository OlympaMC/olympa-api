package fr.olympa.api.spigot.economy;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.spigot.gui.OlympaGUI;
import fr.olympa.api.spigot.item.ItemUtils;
import fr.olympa.api.utils.Prefix;

public abstract class BankGUI<T extends MoneyPlayerInterface> extends OlympaGUI {

	protected final int maxSlots;
	protected final T player;

	protected int playerSlots;
	private boolean change = false;

	public BankGUI(T player, int maxSlots) {
		super("Coffre de " + player.getName(), maxSlots / 9);
		this.player = player;
		this.maxSlots = maxSlots;
		playerSlots = getPlayerSlots();

		ItemStack[] content = getPlayerBankContents();
		for (int i = 0; i < maxSlots; i++) {
			if (i < playerSlots) {
				if (content.length > i) inv.setItem(i, content[i]);
			}else if (i == playerSlots) {
				inv.setItem(i, slotBuyItem());
			}else {
				inv.setItem(i, ItemUtils.item(Material.RED_STAINED_GLASS_PANE, "§cDébloquez les emplacement précédents"));
			}
		}
	}
	
	private ItemStack slotBuyItem() {
		return ItemUtils.item(Material.LIME_STAINED_GLASS_PANE, "§aAcheter le slot : §l" + getSlotPrice(playerSlots) + "$");
	}

	public boolean onClick(Player p, ItemStack current, int slot, ClickType click) {
		if (slot < playerSlots) {
			change = true;
			return false;
		}else if (slot == playerSlots) { // si c'est le slot à acheter
			if (player.getGameMoney().withdraw(getSlotPrice(playerSlots))) { // vérifier si l'achat se fait
				incrementPlayerBankSlots();
				playerSlots++;
				inv.setItem(slot, null); // enlever l'item "Acheter l'emplacement"
				if (playerSlots < maxSlots) inv.setItem(playerSlots, slotBuyItem());
			}else Prefix.DEFAULT_BAD.sendMessage(p, "Vous n'avez pas l'argent suffisant pour acheter cet emplacement.");
			return true;
		}
		return true;
	}
	
	public boolean onClickCursor(Player p, ItemStack current, ItemStack cursor, int slot) {
		if (slot >= playerSlots) return true;
		change = true;
		return false;
	}

	@Override
	public boolean onMoveItem(Player p, ItemStack moved, boolean isFromInv, int slot) {
		change = true;
		return false;
	}

	public boolean onClose(Player p) {
		if (change) {
			ItemStack[] items = new ItemStack[playerSlots];
			for (int i = 0; i < playerSlots; i++) {
				items[i] = inv.getItem(i);
			}
			setPlayerBankContents(items);
		}
		return true;
	}
	
	public double getSlotPrice(int slot) {
		return slot * 30;
	}

	public abstract int getPlayerSlots();

	public abstract ItemStack[] getPlayerBankContents();

	public abstract void setPlayerBankContents(ItemStack[] items);

	public abstract void incrementPlayerBankSlots();

}
