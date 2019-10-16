package fr.olympa.api.item;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.utils.SpigotUtils;

public class OlympaItemBuild implements Cloneable {

	private Material material;
	private int size = 1;
	private Short durability;
	private String name;
	private List<String> lore;
	private ItemFlag[] itemFlags;
	private Boolean unbreakable;

	public OlympaItemBuild(ItemStack itemStack) {
		this.material = itemStack.getType();
		this.size = itemStack.getAmount();
		this.durability = itemStack.getDurability();

		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta.hasDisplayName()) {
			this.name = itemMeta.getDisplayName();
		}

		if (itemMeta.hasLore()) {
			this.lore = itemMeta.getLore();
		}

		Set<ItemFlag> flags = itemMeta.getItemFlags();
		if (!flags.isEmpty()) {
			this.itemFlags = flags.toArray(new ItemFlag[0]);
		}

		if (itemMeta.isUnbreakable()) {
			this.unbreakable = true;
		}
	}

	public OlympaItemBuild(Material material, String name) {
		this.material = material;
		this.name = name;
	}

	public OlympaItemBuild breakable() {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.unbreakable = true;
		return olympaItemBuild;
	}

	public ItemStack build() {
		ItemStack itemStack = new ItemStack(this.material, this.size);
		if (this.durability != null) {
			itemStack.setDurability(this.durability);
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		if (this.name != null) {
			itemMeta.setDisplayName(SpigotUtils.color(this.name));
		}

		if (this.lore != null) {
			itemMeta.setLore(SpigotUtils.color(this.lore));
		}

		if (this.itemFlags != null) {
			itemMeta.addItemFlags(this.itemFlags);
		}

		if (this.unbreakable != null) {
			itemMeta.setUnbreakable(true);
		}
		itemStack.setItemMeta(itemMeta);
		return itemStack;
	}

	@Override
	public OlympaItemBuild clone() {
		try {
			return (OlympaItemBuild) super.clone();
		} catch (final CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	public OlympaItemBuild durability(short durability) {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.durability = durability;
		return olympaItemBuild;
	}

	public OlympaItemBuild flag(ItemFlag... itemFlags) {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.itemFlags = itemFlags;
		return olympaItemBuild;
	}

	public OlympaItemBuild lore(String... lore) {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.lore = Arrays.asList(lore);
		return olympaItemBuild;
	}

	public OlympaItemBuild name(String name) {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.name = name;
		return olympaItemBuild;
	}

	public OlympaItemBuild unbreakable() {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.unbreakable = true;
		return olympaItemBuild;
	}
}
