package fr.olympa.api.item;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
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
	private Integer size;
	@Deprecated
	private Short durability;
	private Integer customModelData;
	private String name;
	private List<String> lore;
	private Map<Enchantment, Integer> enchantments;
	private ItemFlag[] itemFlags;
	private Boolean unbreakable;
	private OfflinePlayer player;

	@SuppressWarnings("deprecation")
	public OlympaItemBuild(ItemStack itemStack) {
		material = itemStack.getType();
		size = itemStack.getAmount();
		durability = itemStack.getDurability();

		customModelData = itemStack.getItemMeta().getCustomModelData();
		enchantments = itemStack.getEnchantments();
		if (enchantments != null && enchantments.isEmpty()) {
			enchantments = null;
		}

		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta.hasDisplayName()) {
			name = itemMeta.getDisplayName();
		}

		if (itemMeta.hasLore()) {
			lore = itemMeta.getLore();
		}

		Set<ItemFlag> flags = itemMeta.getItemFlags();
		if (!flags.isEmpty()) {
			itemFlags = flags.toArray(new ItemFlag[0]);
		}

		if (itemMeta.isUnbreakable()) {
			unbreakable = true;
		}
	}

	public OlympaItemBuild(Material material, String name) {
		this.material = material;
		this.name = name;
	}

	public OlympaItemBuild(String name) {
		this.name = name;
	}

	public OlympaItemBuild addlore(String... lore) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.lore.addAll(Arrays.asList(lore));
		return olympaItemBuild;
	}

	public OlympaItemBuild addName(String name) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.name += name;
		return olympaItemBuild;
	}

	public OlympaItemBuild breakable() {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.unbreakable = true;
		return olympaItemBuild;
	}

	@SuppressWarnings("deprecation")
	public ItemStack build() {
		ItemStack itemStack = new ItemStack(material, 1);
		if (size != null) {
			itemStack.setAmount(size);
		}

		if (durability != null) {
			itemStack.setDurability(durability);
		}
		if (enchantments != null && !enchantments.isEmpty()) {
			if (hasEnchantmentsUnsafe()) {
				itemStack.addUnsafeEnchantments(enchantments);
			} else {
				itemStack.addEnchantments(enchantments);
			}
		}
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (name != null) {
			itemMeta.setDisplayName(SpigotUtils.color(name));
		}
		if (customModelData != null) {
			itemMeta.setCustomModelData(customModelData);
		}
		if (lore != null) {
			itemMeta.setLore(SpigotUtils.color(lore));
		}
		if (itemFlags != null) {
			itemMeta.addItemFlags(itemFlags);
		}
		if (player != null) {
			((SkullMeta) itemMeta).setOwningPlayer(player);
		}
		if (unbreakable != null) {
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

	public OlympaItemBuild customModelData(int customModelData) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.customModelData = customModelData;
		return olympaItemBuild;
	}

	@Deprecated
	public OlympaItemBuild durability(short durability) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.durability = durability;
		return olympaItemBuild;
	}

	public OlympaItemBuild enchantment(Enchantment enchantment, int level) {
		OlympaItemBuild olympaItemBuild = clone();
		if (olympaItemBuild.enchantments == null) {
			olympaItemBuild.enchantments = new HashMap<>();
		}
		olympaItemBuild.enchantments.put(enchantment, level);
		return olympaItemBuild;
	}

	public OlympaItemBuild enchantment(Map<Enchantment, Integer> enchantments) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.enchantments = enchantments;
		return olympaItemBuild;
	}

	public OlympaItemBuild flag(ItemFlag... itemFlags) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.itemFlags = itemFlags;
		return olympaItemBuild;
	}

	private boolean hasEnchantmentsUnsafe() {
		return enchantments != null && enchantments.entrySet().stream().anyMatch(entry -> entry.getValue() > 5);
	}

	public OlympaItemBuild lore(String... lore) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.lore = Arrays.asList(lore);
		return olympaItemBuild;
	}

	public OlympaItemBuild name(String name) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.name = name;
		return olympaItemBuild;
	}

	public void setEnchantments(Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
	}

	public OlympaItemBuild setLore(int i, String lore) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.lore.set(i, olympaItemBuild.lore.get(i) + lore);
		return olympaItemBuild;
	}

	public OlympaItemBuild size(int size) {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.size = size;
		return olympaItemBuild;
	}

	public OlympaItemBuild skullowner(OfflinePlayer player) {
		OlympaItemBuild olympaItemBuild = clone();
		this.player = player;
		material = Material.PLAYER_HEAD;
		return olympaItemBuild;
	}

	public OlympaItemBuild skullowner(String playerName) {
		return skullowner(Bukkit.getOfflinePlayer(playerName));
	}

	public OlympaItemBuild unbreakable() {
		OlympaItemBuild olympaItemBuild = clone();
		olympaItemBuild.unbreakable = true;
		return olympaItemBuild;
	}
}
