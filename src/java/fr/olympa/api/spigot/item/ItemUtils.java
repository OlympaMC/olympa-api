package fr.olympa.api.spigot.item;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import fr.olympa.core.spigot.OlympaCore;

public class ItemUtils {

	public static ImmutableItemStack none = new ImmutableItemStack(item(Material.RED_STAINED_GLASS_PANE, "§cx"));
	public static ImmutableItemStack cancel = new ImmutableItemStack(item(Material.BARRIER, "§cAnnuler"));
	public static ImmutableItemStack done = new ImmutableItemStack(item(Material.DIAMOND, "§b§lValider"));
	public static ImmutableItemStack error = new ImmutableItemStack(item(Material.BARRIER, "§c§lerreur"));

	private static Map<String, String> textures = new HashMap<>();

	/**
	 * Create an ItemStack instance
	 * @param type material type
	 * @param name name of the item
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack item(Material type, String name, String... lore) {
		return item(type, null, name, lore);
	}
	
	/**
	 * Create an ItemStack instance
	 * @param type material type
	 * @param customModelData custom model data, or <code>null</code>
	 * @param name name of the item
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack item(Material type, Integer customModelData, String name, String... lore) {
		ItemStack is = new ItemStack(type);
		ItemMeta im = is.getItemMeta();
		im.setDisplayName(name);
		im.addItemFlags(ItemFlag.values());
		if (customModelData != null) im.setCustomModelData(customModelData);
		is.setItemMeta(im);
		if (lore != null && lore.length != 0)
			lore(is, lore);
		return is;
	}

	/**
	 * Create an ItemStack instance of a skull item
	 * @param callback consumer called when the head is ready
	 * @param name name of the item
	 * @param skull skull's owner name
	 * @param lore lore of the item, formatted as a String array
	 */
	public static void skull(Consumer<ItemStack> callback, String name, String skull, String... lore) {
		Bukkit.getScheduler().runTaskAsynchronously(OlympaCore.getInstance(), () -> {
			String value = textures.get(skull);
			if (value == null) {
				value = getHeadValue(skull);
				textures.put(skull, value);
			}
			callback.accept(skullCustom(name, value, lore));
		});
	}

	public static void skull(Consumer<SkullMeta> callback, ItemStack item, String skull) {
		Bukkit.getScheduler().runTaskAsynchronously(OlympaCore.getInstance(), () -> {
			String value = textures.get(skull);
			if (value == null) {
				value = getHeadValue(skull);
				textures.put(skull, value);
			}
			callback.accept(skullCustom((SkullMeta) item.getItemMeta(), value));
		});
	}

	public static String getHeadValue(String name) {
		try {
			String result = getURLContent("https://api.mojang.com/users/profiles/minecraft/" + name);
			Gson g = new Gson();
			JsonObject obj = g.fromJson(result, JsonObject.class);
			if (obj == null || !obj.has("id"))
				return null;
			String uid = obj.get("id").toString().replace("\"", "");
			String signature = getURLContent("https://sessionserver.mojang.com/session/minecraft/profile/" + uid);
			obj = g.fromJson(signature, JsonObject.class);
			String value = obj.getAsJsonArray("properties").get(0).getAsJsonObject().get("value").getAsString();
			String decoded = new String(Base64.getDecoder().decode(value));
			obj = g.fromJson(decoded, JsonObject.class);
			String skinURL = obj.getAsJsonObject("textures").getAsJsonObject("SKIN").get("url").getAsString();
			byte[] skinByte = ("{\"textures\":{\"SKIN\":{\"url\":\"" + skinURL + "\"}}}").getBytes();
			return new String(Base64.getEncoder().encode(skinByte));
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	private static String getURLContent(String urlStr) {
		URL url;
		BufferedReader in = null;
		StringBuilder sb = new StringBuilder();
		try {
			url = new URL(urlStr);
			in = new BufferedReader(new InputStreamReader(url.openStream(), StandardCharsets.UTF_8));
			String str;
			while ((str = in.readLine()) != null)
				sb.append(str);
		} catch (Exception ignored) {} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException ignored) {}
		}
		return sb.toString();
	}

	/**
	 * Create an ItemStack instance of a skull item
	 * @param name name of the item
	 * @param value texture value
	 * @param lore lore of the item, formatted as a String array
	 * @return the ItemStack instance
	 */
	public static ItemStack skullCustom(String name, String value, String... lore) {
		ItemStack is = new ItemStack(Material.PLAYER_HEAD);
		SkullMeta im;
		if (lore != null && lore.length != 0)
			is = lore(is, lore);
		im = (SkullMeta) is.getItemMeta();
		im.setDisplayName(name);
		skullCustom(im, value);
		is.setItemMeta(im);
		return is;
	}

	public static SkullMeta skullCustom(SkullMeta im, String value) {
		if (value != null) {
			GameProfile profile = new GameProfile(UUID.randomUUID(), null);
			profile.getProperties().put("textures", new Property("textures", value, null));
			try {
				Method setProfile = im.getClass().getDeclaredMethod("setProfile", GameProfile.class);
				setProfile.setAccessible(true);
				setProfile.invoke(im, profile);
			} catch (ReflectiveOperationException e) {
				e.printStackTrace();
			}
		}
		return im;
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
		if (lore != null && lore.length != 0)
			for (String s : lore) {
				if (s == null)
					continue;
				List<String> lss = new ArrayList<>();
				String[] splitted = StringUtils.splitByWholeSeparator(s, "\\n");
				if (splitted.length == 0) {
					ls.add("");
					continue;
				}
				for (String as : splitted)
					lss.add(as);
				String last = "";
				for (String ss : lss) {
					ss = last + ss;
					int i = ss.lastIndexOf("§");
					if (i != -1)
						last = ss.charAt(i) + "" + ss.charAt(i + 1);
					ls.add(ss);
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
	public static ItemStack loreAdd(ItemStack is, String... add) {
		if (!is.getItemMeta().hasLore()) {
			lore(is, add);
			return is;
		}
		List<String> ls = is.getItemMeta().getLore();
		ls.addAll(Arrays.asList(add));
		lore(is, ls.toArray(new String[0]));
		return is;
	}

	public static ItemStack setRawLore(ItemStack item, List<String> lore) {
		ItemMeta meta = item.getItemMeta();
		meta.setLore(lore);
		item.setItemMeta(meta);
		return item;
	}

	public static String[] getLore(ItemStack is) {
		if (!is.hasItemMeta())
			return null;
		ItemMeta meta = is.getItemMeta();
		if (!meta.hasLore())
			return null;
		return meta.getLore().toArray(new String[0]);
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
		if (is.getType() != Material.PLAYER_HEAD)
			return null;
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
	public static String getName(ItemStack is) {
		if (is == null)
			return null;
		if (is.hasItemMeta()) {
			ItemMeta meta = is.getItemMeta();
			if (meta.hasDisplayName())
				return meta.getDisplayName();
		}
		return is.getType().name();
	}

	public static boolean hasEnchant(ItemStack is, Enchantment en) {
		return is.getItemMeta().hasEnchant(en);
	}

	public static ItemStack addEnchant(ItemStack is, Enchantment en, int level) {
		ItemMeta im = is.getItemMeta();
		im.addEnchant(en, level, true);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack removeEnchant(ItemStack is, Enchantment en) {
		ItemMeta im = is.getItemMeta();
		im.removeEnchant(en);
		is.setItemMeta(im);
		return is;
	}

	public static ItemStack itemSeparator(DyeColor color) {
		return item(Material.valueOf(color.name() + "_STAINED_GLASS_PANE"), "§a");
	}

	/**
	 * Get a "switch" item : ink sack
	 * @param name name of the item
	 * @param enabled is the switch enabled by default
	 * @param lore lore of the item
	 * @return ItemStack instance of the created switch
	 */
	public static ItemStack itemSwitch(String name, boolean enabled, String... lore) {
		return item(enabled ? Material.LIME_DYE : Material.GRAY_DYE, (enabled ? "§a" : "§7") + name, lore);
	}

	/**
	 * Toggle a switch item, created with {@link #itemSwitch(String, boolean, String...)}
	 * @param itemSwitch switch item
	 * @return new state of the switch
	 */
	public static boolean toggle(ItemStack itemSwitch) {
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
	public static boolean set(ItemStack itemSwitch, boolean enable) {
		if (itemSwitch == null)
			return enable;
		String name = getName(itemSwitch);
		name(itemSwitch, (enable ? "§a" : "§7") + name.substring(2));
		itemSwitch.setType(enable ? Material.LIME_DYE : Material.GRAY_DYE);
		return enable;
	}

	public static byte[] serializeItemsArray(ItemStack[] items) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

		dataOutput.writeInt(items.length);

		for (ItemStack item : items)
			dataOutput.writeObject(item);

		dataOutput.close();
		return outputStream.toByteArray();
	}

	public static ItemStack[] deserializeItemsArray(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null || bytes.length == 0)
			return new ItemStack[0];
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
		ItemStack[] items = new ItemStack[dataInput.readInt()];

		// Read the serialized inventory
		for (int i = 0; i < items.length; i++)
			items[i] = (ItemStack) dataInput.readObject();

		dataInput.close();
		return items;
	}

	public static int getInventoryContentsLength(Inventory inv) {
		int length = 0;
		for (ItemStack item : inv.getContents())
			if (item != null)
				length++;
		return length;
	}

}
