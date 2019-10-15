package fr.olympa.api.item;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ItemUtils {
	
	/**
	 * Create an ItemStack instance
	 * @param type material type
	 * @param name name of the item
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack item(Material type, String name, String... lore){
		ItemStack is = new ItemStack(type);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.addItemFlags(ItemFlag.values());
		is.setItemMeta(im);
		if (lore != null && lore.length != 0) lore(is, lore);
		return is;
	}
	
	/**
	 * Create an ItemStack instance of a skull item
	 * @param name name of the item
	 * @param skull skull's owner name
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack skull(String name, String skull, String... lore) {
		ItemStack is = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta im = (SkullMeta) is.getItemMeta();
		im.setDisplayName(name);
		im.setOwner(skull);
		is.setItemMeta(im);
		if (lore != null && lore.length != 0) lore(is, lore);
		return is;
	}

	/**
	 * Set the lore of an item (override old lore)
	 * @param is ItemStack instance to edit
	 * @param lore new lore of the item, formatted as a String array
	 * @return the same ItemStack instance, with the new lore
	 */
	public static ItemStack lore(ItemStack is, String... lore) {
		ItemMeta im = is.getItemMeta();
		List<String> ls = new ArrayList<>();
		if (lore != null && lore.length != 0){
			for (String s : lore){
				if (s == null) {
					ls.add("");
					continue;
				}
				List<String> lss = new ArrayList<>();
				String[] splitted = StringUtils.splitByWholeSeparator(s, "\\n");
				if (splitted.length == 0) {
					ls.add("");
					continue;
				}
				for (String as : splitted) {
					lss.add(as);
				}
				String last = "";
				for (String ss : lss){
					ss = last + ss;
					int i = ss.lastIndexOf("§");
					if (i != -1) last = ss.charAt(i) + "" + ss.charAt(i + 1);
					ls.add(ss);
				}
			}
		}
		im.setLore(ls);
		is.setItemMeta(im);
		
		return is;
	}
	
	/**
	 * Add some lore of an ItemStack instance, and keep the old lore
	 * @param is ItemStack instance to edit
	 * @param add lore to add, formatted as a String array
	 * @return the same ItemStack instance, with the new lore added at the end
	 */
	public static ItemStack loreAdd(ItemStack is, String... add){
		if (!is.getItemMeta().hasLore()){
			lore(is, add);
			return is;
		}
		List<String> ls = new ArrayList<>(Arrays.asList(getLore(is)));
		ls.addAll(Arrays.asList(add));
		lore(is, ls.toArray(new String[0]));
		return is;
	}

	public static String[] getLore(ItemStack is) {
		if (!is.getItemMeta().hasLore()) return null;
		return is.getItemMeta().getLore().toArray(new String[0]);
	}

	/**
	 * Change the owner of an skull ItemStack
	 * @param is skull ItemStack instance
	 * @param ownerName new owner name
	 * @return same ItemStack instance, with skull's owner changed
	 */
	public static ItemStack owner(ItemStack is, String ownerName) {
		Validate.isTrue(is.getItemMeta() instanceof SkullMeta, "ItemStack must be a Skull");
		SkullMeta im = (SkullMeta) is.getItemMeta();
		im.setOwner(ownerName);
		is.setItemMeta(im);
		return is;
	}

	public static String getOwner(ItemStack is) {
		if (is.getType() != Material.PLAYER_HEAD) return null;
		return ((SkullMeta) is.getItemMeta()).getOwner();
	}

	/**
	 * Change the name of an ItemStack instance
	 * @param is ItemStack instance to edit
	 * @param name new name of the item
	 * @return same ItemStack instance with the new name
	 */
	public static ItemStack name(ItemStack is, String name) {
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		is.setItemMeta(im);
		return is;
	}
	
	/**
	 * Add a string at the end of the name of an ItemStack instance
	 * @param is ItemStack instance to edit
	 * @param add String to add at the end of existant name
	 * @return same ItemStack instance with edited name
	 */
	public static ItemStack nameAdd(ItemStack is, String add) {
		return name(is, getName(is) + add);
	}
	
	/**
	 * Get the name of an ItemStack (if no custom name, then it will return the material name)
	 * @param is ItemStack instance
	 * @return the name of the ItemStack
	 */
	public static String getName(ItemStack is){
		if (is == null) return null;
		if (!is.hasItemMeta() || !is.getItemMeta().hasDisplayName()) return is.getType().name();
		return is.getItemMeta().getDisplayName();
	}
	
	public static boolean hasEnchant(ItemStack is, Enchantment en){
		return is.getItemMeta().hasEnchant(en);
	}
	
	public static ItemStack addEnchant(ItemStack is, Enchantment en, int level){
		ItemMeta im = is.getItemMeta();
		im.addEnchant(en, level, true);
		is.setItemMeta(im);
		return is;
	}
	
	public static ItemStack removeEnchant(ItemStack is, Enchantment en){
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(en);
		is.setItemMeta(im);
		return is;
	}
	


	/**
	 * Get a "switch" item : ink sack
	 * @param name name of the item
	 * @param enabled is the switch enabled by default
	 * @param lore lore of the item
	 * @return ItemStack instance of the created switch
	 */
	public static ItemStack itemSwitch(String name, boolean enabled, String... lore){
		return item(enabled ? Material.LIME_DYE : Material.GRAY_DYE, (enabled ? "§a" : "§7") + name, lore);
	}
	
	/**
	 * Toggle a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @param itemSwitch switch item
	 * @return new state of the switch
	 */
	public static boolean toggle(ItemStack itemSwitch){
		String name = getName(itemSwitch);
		boolean toggled = name.charAt(1) != 'a'; // toggling
		return set(itemSwitch, toggled);
	}
	
	/**
	 * Set the state of a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @see #toggle(ItemStack)
	 * @param itemSwitch switch item
	 * @param enable new state of the switch
	 * @return same state
	 */
	public static boolean set(ItemStack itemSwitch, boolean enable){
		if (itemSwitch == null) return enable;
		String name = getName(itemSwitch);
		name(itemSwitch, (enable ? "§a" : "§7") + name.substring(2));
		itemSwitch.setType(enable ? Material.LIME_DYE : Material.GRAY_DYE);
		return enable;
	}
	
}
