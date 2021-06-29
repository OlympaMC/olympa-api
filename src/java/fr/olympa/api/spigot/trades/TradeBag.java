package fr.olympa.api.spigot.trades;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import fr.olympa.api.common.observable.AbstractObservable;
import fr.olympa.api.utils.Prefix;

public class TradeBag<T extends TradePlayerInterface> extends AbstractObservable {

	private Collection<ItemStack> items = new ArrayList<>(0);
	private T p;

	public TradeBag(T p) {
		this.p = p;
	}

	public void setItems(ItemStack... list) {
		setItems(Arrays.asList(list));
	}

	public void setItems(Collection<ItemStack> list) {
		if (items.size() == 0 && list.size() == 0)
			return;

		if (list != null)
			items = list;
		else
			items = new ArrayList<>(0);
		update();
	}

	public Collection<ItemStack> getItems() {
		return Collections.unmodifiableCollection(items);
	}

	public void flushBag() {
		if (isEmpty()) {
			Prefix.DEFAULT_GOOD.sendMessage((Player) p.getPlayer(), "§7Tu as déjà récupéré tous tes objets.");
			return;
		}

		Collection<ItemStack> its = ((Player) p.getPlayer()).getInventory().addItem(items.toArray(new ItemStack[items.size()])).values();
		setItems(its);

		if (its.size() > 0)
			Prefix.DEFAULT_GOOD.sendMessage((Player) p.getPlayer(), "Tu as récupéré certains de tes objets.");
		else
			Prefix.DEFAULT_GOOD.sendMessage((Player) p.getPlayer(), "Tu as récupéré tous tes objets !");
	}

	public boolean isEmpty() {
		return items.size() == 0;
	}
}
