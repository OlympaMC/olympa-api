package fr.olympa.api.item;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import fr.olympa.api.utils.SpigotUtils;

public class OlympaItemBuild implements Cloneable {

	private Material material;
	private int size = 1;
	private Short durability;
	private String name;
	private List<String> lore;
	private Map<Enchantment, Integer> enchantments;
	private Boolean enchantmentsUnsafe;
	private ItemFlag[] itemFlags;
	private Boolean unbreakable;
	private OfflinePlayer player;

	public OlympaItemBuild(ItemStack itemStack) {
		this.material = itemStack.getType();
		this.size = itemStack.getAmount();
		this.durability = itemStack.getDurability();
		this.enchantments = itemStack.getEnchantments();
		if (this.enchantments != null && this.enchantments.isEmpty()) {
			if (this.enchantments.isEmpty()) {
				this.enchantments = null;
			} else if (this.enchantments.entrySet().stream().anyMatch(entry -> entry.getValue() > 10)) {
				this.enchantmentsUnsafe = true;
			}
		}

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

	public OlympaItemBuild(String name) {
		this.name = name;
	}

	public OlympaItemBuild addName(String name) {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.name += name;
		return olympaItemBuild;
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

		if (this.enchantments != null && !this.enchantments.isEmpty()) {
			if (this.enchantmentsUnsafe != null && this.enchantmentsUnsafe) {
				itemStack.addUnsafeEnchantments(this.enchantments);
			} else {
				itemStack.addEnchantments(this.enchantments);
			}
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

		if (this.player != null) {
			((SkullMeta) itemMeta).setOwningPlayer(this.player);
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

	public OlympaItemBuild setLore(int i, String lore) {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.lore.set(i, olympaItemBuild.lore.get(i) + lore);
		return olympaItemBuild;
	}

	public OlympaItemBuild skullowner(OfflinePlayer player) {
		OlympaItemBuild olympaItemBuild = this.clone();
		this.player = player;
		this.material = Material.PLAYER_HEAD;
		return olympaItemBuild;
	}

	public OlympaItemBuild unbreakable() {
		OlympaItemBuild olympaItemBuild = this.clone();
		olympaItemBuild.unbreakable = true;
		return olympaItemBuild;
	}
}
