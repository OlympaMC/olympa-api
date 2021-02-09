package fr.olympa.api.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import fr.olympa.api.chat.ColorUtils;

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
	private String player;

	//	private ItemStack cache;

	@SuppressWarnings("deprecation")
	public OlympaItemBuild(ItemStack itemStack) {
		material = itemStack.getType();
		size = itemStack.getAmount();
		durability = itemStack.getDurability();

		customModelData = itemStack.getItemMeta().getCustomModelData();
		enchantments = itemStack.getEnchantments();
		if (enchantments != null && enchantments.isEmpty())
			enchantments = null;

		ItemMeta itemMeta = itemStack.getItemMeta();
		if (itemMeta.hasDisplayName())
			name = itemMeta.getDisplayName();

		if (itemMeta.hasLore())
			lore = itemMeta.getLore();

		Set<ItemFlag> flags = itemMeta.getItemFlags();
		if (!flags.isEmpty())
			itemFlags = flags.toArray(new ItemFlag[0]);

		if (itemMeta.isUnbreakable())
			unbreakable = true;
	}

	public OlympaItemBuild(Material material, String name) {
		this.material = material;
		this.name = name;
	}

	public OlympaItemBuild(String name) {
		this.name = name;
	}

	public OlympaItemBuild addLore(String... lore) {
		if (this.lore == null)
			this.lore = new ArrayList<>();
		for (String l : lore)
			this.lore.add(l);
		//		cache = null;
		return this;
	}

	public OlympaItemBuild addLoreBefore(String... lore) {
		if (this.lore == null)
			this.lore = new ArrayList<>();
		for (int i = 0; lore.length > i; i++)
			this.lore.add(0, lore[lore.length - 1 - i]);
		//		cache = null;
		return this;
	}

	public OlympaItemBuild addName(String name) {
		this.name += name;
		//		cache = null;
		return this;
	}

	public OlympaItemBuild breakable() {
		unbreakable = true;
		//		cache = null;
		return this;
	}

	@SuppressWarnings("deprecation")
	public ItemStack build() {
		//		if (cache != null)
		//			return cache;
		ItemStack itemStack = new ItemStack(material, 1);
		if (size != null)
			itemStack.setAmount(size);

		if (durability != null)
			itemStack.setDurability(durability);
		if (enchantments != null && !enchantments.isEmpty())
			if (hasEnchantmentsUnsafe())
				itemStack.addUnsafeEnchantments(enchantments);
			else
				itemStack.addEnchantments(enchantments);
		ItemMeta itemMeta = itemStack.getItemMeta();
		if (name != null)
			itemMeta.setDisplayName(ColorUtils.color(name));
		if (customModelData != null)
			itemMeta.setCustomModelData(customModelData);
		if (lore != null)
			itemMeta.setLore(ColorUtils.color(lore));
		if (itemFlags != null)
			itemMeta.addItemFlags(itemFlags);
		if (unbreakable != null)
			itemMeta.setUnbreakable(true);
		if (player != null)
			ItemUtils.skull(newItemMeta -> {
				itemStack.setItemMeta(newItemMeta);
			}, itemStack, player);
		//			((SkullMeta) itemMeta).setOwningPlayer(player);
		itemStack.setItemMeta(itemMeta);
		//		cache = itemStack;
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
		this.customModelData = customModelData;
		//		cache = null;
		return this;
	}

	@Deprecated
	public OlympaItemBuild durability(short durability) {
		this.durability = durability;
		//		cache = null;
		return this;
	}

	public OlympaItemBuild enchantment(Enchantment enchantment, int level) {
		if (enchantments == null)
			enchantments = new HashMap<>();
		enchantments.put(enchantment, level);
		//		cache = null;
		return this;
	}

	public OlympaItemBuild enchantment(Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
		//		cache = null;
		return this;
	}

	public OlympaItemBuild flag(ItemFlag... itemFlags) {
		this.itemFlags = itemFlags;
		//		cache = null;
		return this;
	}

	private boolean hasEnchantmentsUnsafe() {
		return enchantments != null && enchantments.entrySet().stream().anyMatch(entry -> entry.getValue() > 5);
	}

	public OlympaItemBuild lore(String... lore) {
		if (this.lore == null)
			this.lore = new ArrayList<>();
		for (String l : lore)
			this.lore.add(l);
		//		cache = null;
		return this;
	}

	public OlympaItemBuild name(String name) {
		this.name = name;
		//		cache = null;
		return this;
	}

	public void setEnchantments(Map<Enchantment, Integer> enchantments) {
		this.enchantments = enchantments;
		//		cache = null;
	}

	public OlympaItemBuild setLore(int i, String lore) {
		if (this.lore == null)
			this.lore = new ArrayList<>();
		this.lore.set(i, this.lore.get(i) + lore);
		//		cache = null;
		return this;
	}

	public OlympaItemBuild size(int size) {
		this.size = size;
		//		cache = null;
		return this;
	}

	public OlympaItemBuild skullowner(String player) {
		this.player = player;
		material = Material.PLAYER_HEAD;
		//		cache = null;
		return this;
	}

	public OlympaItemBuild skullowner(OfflinePlayer player) {
		return skullowner(player.getName());
	}

	//	@SuppressWarnings("deprecation")
	//	public OlympaItemBuild skullowner(String playerName) {
	//		return skullowner(Bukkit.getOfflinePlayer(playerName));
	//	}

	public OlympaItemBuild unbreakable() {
		unbreakable = true;
		//		cache = null;
		return this;
	}
}
