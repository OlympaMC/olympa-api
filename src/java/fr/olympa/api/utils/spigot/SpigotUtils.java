package fr.olympa.api.utils.spigot;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.Region;
import fr.olympa.api.region.shapes.Cuboid;
import fr.olympa.api.utils.Prefix;

public class SpigotUtils {

	private static final BlockFace[] axis = { BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST };
	private static final BlockFace[] radial = { BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST };

	public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
		if (useSubCardinalDirections)
			return radial[Math.round(yaw / 45f) & 0x7].getOppositeFace();

		return axis[Math.round(yaw / 90f) & 0x3].getOppositeFace();
	}

	public static Location addYToLocation(Location location, float y) {
		return new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ(), location.getYaw(), location.getPitch());
	}
	
	public static Location normalizeLocationToBlock(Location location) {
		return new Location(location.getWorld(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
	}

	public static int clearPlayer(Player player) {
		PlayerInventory inventory = player.getInventory();
		int size = (int) (Arrays.stream(inventory.getContents()).filter(item -> item != null).count() + Arrays.stream(inventory.getArmorContents()).filter(item -> item != null).count());
		inventory.clear();
		inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
		//		inventory.setExtraContents(new ItemStack[inventory.getExtraContents().length]);
		return size;
	}

	public static void deletePlayerLocalData(Player player) {
		String worldName = Bukkit.getWorlds().get(0).getName();
		String playerUuid = player.getUniqueId().toString();
		File worldDir = new File(Bukkit.getServer().getWorldContainer().getPath() + "/" + worldName);
		new File(worldDir, "playerdata/" + playerUuid + ".dat").delete();
		new File(worldDir, "advancements/" + playerUuid + ".json").delete();
		File stats = new File(worldDir, "stats/" + playerUuid + ".json");
		if (stats.exists())
			stats.delete();
	}

	public static String connectScreen(String s) {
		return ColorUtils.color("\n&e&m-------------------------------------------\n\n&e[&6Olympa&e]\n\n" + s + "\n\n&e&m-------------------------------------------");
	}

	public static String convertBlockLocationToString(Location loc) {
		return loc.getWorld().getName() + " " + loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
	}

	private static final NumberFormat HUMAN_FORMAT = new DecimalFormat(".##");
	
	public static String convertLocationToHumanString(Location loc) {
		return loc.getWorld().getName() + " " + HUMAN_FORMAT.format(loc.getX()) + " " + HUMAN_FORMAT.format(loc.getY()) + " " + HUMAN_FORMAT.format(loc.getZ());
	}
	
	public static String convertLocationToString(Location loc) {
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		if ((int) loc.getPitch() != 0 || (int) loc.getYaw() != 0) {
			int yaw = (int) loc.getYaw();
			int pitch = (int) loc.getPitch();
			return world + " " + x + " " + y + " " + z + " " + yaw + " " + pitch;
		}
		return world + " " + x + " " + y + " " + z;
	}

	public static Location convertStringToLocation(String loc) {
		if (loc != null) {
			String[] coords = loc.split(" ");
			World w = Bukkit.getWorld(coords[0]);
			double x = Double.parseDouble(coords[1]);
			double y = Double.parseDouble(coords[2]);
			double z = Double.parseDouble(coords[3]);
			if (coords.length == 6) {
				float yaw = Float.parseFloat(coords[4]);
				float pitch = Float.parseFloat(coords[5]);
				return new Location(w, x, y, z, yaw, pitch);
			}
			return new Location(w, x, y, z);
		}
		return null;
	}

	public static List<Location> getBlockAround(Location location, int raduis) {
		List<Location> locations = new ArrayList<>();
		for (int x = raduis; x >= -raduis; x--)
			for (int y = raduis; y >= -raduis; y--)
				for (int z = raduis; z >= -raduis; z--)
					locations.add(location.getBlock().getRelative(x, y, z).getLocation());
		return locations;
	}

	public static Region getCuboid(ConfigurationSection section, String key) {
		if (section != null) {
			Location loc1 = convertStringToLocation(section.getString(key + ".pos1"));
			Location loc2 = convertStringToLocation(section.getString(key + ".pos2"));
			Region cuboid = new Cuboid(loc1, loc2);
			return cuboid;
		}
		return null;
	}

	public static Location getFirstBlockUnderPlayer(Player player) {
		Location location = player.getLocation();
		do {
			if (location.getBlockY() == 0)
				return null;
			location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		} while (!location.getBlock().getType().isOccluding());
		return location;
	}

	public static ChatColor getIntervalChatColor(int i, int min, int max) {
		if (i == 0)
			return ChatColor.GRAY;
		if (i < min)
			return ChatColor.GREEN;
		if (i < max)
			return ChatColor.GOLD;
		return ChatColor.RED;
	}

	public static short getIntervalGlassPaneColor(int i, int min, int max) {
		if (i == 0)
			return 0;
		if (i < min)
			return 5;
		if (i < max)
			return 1;
		return 14;
	}

	public static String getName(UUID playerUniqueId) {
		Player player = Bukkit.getPlayer(playerUniqueId);
		if (player != null)
			return player.getName();
		try {
			OlympaPlayer olympaPlayer = new AccountProvider(playerUniqueId).get();
			if (olympaPlayer != null)
				return olympaPlayer.getName();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Player getNearestPlayer(Player checkNear) {
		Player nearest = null;
		for (Player p : checkNear.getWorld().getPlayers())
			if (nearest == null)
				nearest = p;
			else if (p.getLocation().distance(checkNear.getLocation()) < nearest.getLocation().distance(checkNear.getLocation()))
				nearest = p;
		return nearest;
	}

	public static boolean hasEnoughPlace(Inventory inventory, ItemStack... items) {
		Inventory inventory2 = Bukkit.createInventory(null, inventory.getSize());
		inventory2.setContents(inventory.getContents());
		int amount1 = 0;
		for (ItemStack item : inventory2.getContents())
			if (item != null)
				amount1 += item.getAmount();

		int amount2 = 0;
		for (ItemStack item : items)
			amount2 += item.getAmount();

		int amount3 = amount1 + amount2;
		inventory2.addItem(items);

		int amount4 = 0;
		for (ItemStack item : inventory2.getContents())
			if (item != null)
				amount4 += item.getAmount();

		if (amount4 == amount3)
			return true;

		return false;
	}

	public static boolean isSameLocation(Location location1, Location location2) {
		return location1.getWorld() == location2.getWorld() && location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY() && location1.getBlockZ() == location2.getBlockZ();
	}

	public static boolean isSameChunk(Chunk chunk1, Chunk chunk2) {
		return chunk1.getWorld() == chunk2.getWorld() && chunk1.getX() == chunk2.getX() && chunk1.getZ() == chunk2.getZ();
	}

	public static boolean isSameLocationXZ(Location location1, Location location2) {
		return location1.getWorld() == location2.getWorld() && location1.getBlockX() == location2.getBlockX() && location1.getBlockZ() == location2.getBlockZ();
	}

	public static boolean isSamePlayer(Player player, Player target) {
		return player.getUniqueId().equals(target.getUniqueId());
	}

	public static boolean playerisIn(Player player, Location location) {
		Location playerLocation = player.getLocation();
		return playerLocation.getBlockX() == location.getBlockX() && (playerLocation.getBlockY() == location.getBlockY() || playerLocation.getBlockY() + 1 == location.getBlockY()) && playerLocation.getBlockZ() == location.getBlockZ();
	}

	private static char[] arrows = new char[] { '↑', '↗', '↗', '→', '→', '↘', '↘', '↓', '↓', '↙', '↙', '←', '←', '↖', '↖', '↑' };
	private static final double piOver16 = Math.PI / 16D;
	private static final double pi2 = Math.PI * 2;

	public static char getDirectionToLocation(Player player, Location target) {
		if (player.getWorld() != target.getWorld())
			return 'x';
		Location source = player.getLocation();
		Vector inBetween = target.clone().subtract(source).toVector();
		Vector lookVec = source.getDirection();

		double angleDir = Math.atan2(inBetween.getZ(), inBetween.getX());
		double angleLook = Math.atan2(lookVec.getZ(), lookVec.getX());

		double angle = (angleDir - angleLook + pi2) % pi2 / 2;
		return arrows[(int) Math.floor(angle / piOver16)];
	}

	public static <T extends ConfigurationSerializable> byte[] serialize(T object) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

		dataOutput.writeObject(object);

		dataOutput.close();
		return outputStream.toByteArray();
	}

	public static <T extends ConfigurationSerializable> T deserialize(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);
		BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

		T object = (T) dataInput.readObject();

		dataInput.close();
		return object;
	}

	public static boolean containsItems(Inventory inv, ItemStack i, int amount) {
		if (amount <= 0) return true;
		for (ItemStack item : inv.getContents()) {
			if (item == null)
				continue;
			if (i.isSimilar(item)) {
				if (item.getAmount() >= amount)
					return true;
				else
					amount -= item.getAmount();
			}
		}
		return false;
	}

	public static boolean removeItems(Inventory inv, ItemStack i, int amount) {
		if (amount <= 0)
			throw new IllegalArgumentException("Item cannot have a negative or zero amount");
		ItemStack[] items = inv.getContents();
		for (int slot = 0; slot < items.length; slot++) {
			ItemStack item = items[slot];
			if (item == null)
				continue;
			if (i.isSimilar(item)) {
				if (item.getAmount() == amount) {
					inv.setItem(slot, null);
					return true;
				}
				if (item.getAmount() > amount) {
					item.setAmount(item.getAmount() - amount);
					return true;
				}else if (item.getAmount() < amount) {
					amount -= item.getAmount();
					inv.setItem(slot, null);
				}
			}
		}
		return false;
	}

	public static int getItemAmount(Inventory inv, ItemStack i) {
		int amount = 0;
		for (ItemStack item : inv.getContents()) {
			if (item == null)
				continue;
			if (i.isSimilar(item)) {
				amount += item.getAmount();
				continue;
			}
		}
		return amount;
	}

	public static void giveItems(Player p, ItemStack... items) {
		HashMap<Integer, ItemStack> leftover = p.getInventory().addItem(items);
		if (leftover.isEmpty())
			return;
		leftover.forEach((i, item) -> p.getWorld().dropItem(p.getLocation(), item));
		Prefix.DEFAULT.sendMessage(p, "Tes items ont été jetés au sol car tu n'as plus de place dans ton inventaire !");
	}
	
	/**
	* Breaks a raw string up into a series of lines. Words are wrapped using
	* spaces as decimeters and the newline character is respected.
	*
	* @param rawString The raw string to break.
	* @param lineLength The length of a line of text.
	* @return An array of word-wrapped lines.
	*/
	public static List<String> wordWrap(String rawString, int lineLength) {
		// A null string is a single line
		if (rawString == null) {
			return Arrays.asList("");
		}
		
		rawString = rawString.replace("{nl}", "\n");
		
		// A string shorter than the lineWidth is a single line
		if (rawString.length() <= lineLength && !rawString.contains("\n")) {
			return Arrays.asList(rawString);
		}
		
		char[] rawChars = (rawString + ' ').toCharArray(); // add a trailing space to trigger pagination
		StringBuilder word = new StringBuilder();
		StringBuilder line = new StringBuilder();
		String colors = "";
		boolean colorsSkip = true;
		List<String> lines = new LinkedList<String>();
		
		for (int i = 0; i < rawChars.length; i++) {
			char c = rawChars[i];
			
			// skip chat color modifiers
			if (c == ChatColor.COLOR_CHAR) {
				String color = ChatColor.getByChar(rawChars[i + 1]).toString();
				word.append(color);
				colors = ChatColor.getLastColors(colors + color);
				i++; // Eat the next character as we have already processed it
				colorsSkip = true;
				continue;
			}
			
			if (c == ' ' || c == '\n') {
				if (line.length() == 0 && word.length() > lineLength) { // special case: extremely long word begins a line
					//System.out.println("long word : " + word);
					for (String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
						lines.add(partialWord);
					}
				}else if (line.length() + 1 + word.length() == lineLength) { // Line exactly the correct length...newline
					//System.out.println("good length");
					if (line.length() > 0) {
						line.append(' ');
					}
					line.append(word);
					lines.add(line.toString());
					line = new StringBuilder();
					line.append(colors);
				}else if (line.length() + word.length() >= lineLength) { // Line too long...break the line
					//System.out.println("too long " + line.toString() + " | plus : " + word.toString());
					for (String partialWord : word.toString().split("(?<=\\G.{" + lineLength + "})")) {
						lines.add(line.toString());
						//System.out.println("BREAK " + line.toString() + " | add : " + partialWord);
						line = new StringBuilder(partialWord);
						if (colorsSkip) {
							colorsSkip = false;
						}else {
							line.insert(0, colors);
						}
					}
				}else {
					if (line.length() > 0) {
						line.append(' ');
					}
					line.append(word);
				}
				word = new StringBuilder();
				
				if (c == '\n') { // Newline forces the line to flush
					lines.add(line.toString());
					line = new StringBuilder();
					line.append(colors);
				}
				colorsSkip = false;
			}else {
				word.append(c);
			}
		}
		
		if (line.length() > 0) { // Only add the last line if there is anything to add
			lines.add(line.toString());
		}
		
		// Iterate over the wrapped lines, applying the last color from one line to the beginning of the next
		/*if (lines.get(0).length() == 0 || lines.get(0).charAt(0) != ChatColor.COLOR_CHAR) {
			lines.set(0, ChatColor.WHITE + lines.get(0));
		}*/
		/*for (int i = 1; i < lines.size(); i++) {
			final String pLine = lines.get(i - 1);
			final String subLine = lines.get(i);
			
			//String color = ChatColor.getByChar(pLine.charAt(pLine.lastIndexOf(ChatColor.COLOR_CHAR) + 1));
			String color = ChatColor.getLastColors(pLine);
			if (subLine.length() == 0 || subLine.charAt(0) != ChatColor.COLOR_CHAR) {
				lines.set(i, color + subLine);
			}
		}*/
		
		return lines;
	}
	
	public static List<String> wrapAndAlign(String text, int lineLength) {
		List<String> list = new ArrayList<>();
		List<String> wrapped = SpigotUtils.wordWrap(text, 35);
		for (int i = 0; i < wrapped.size(); i++) {
			list.add((i == 0 ? "§8> §7" : "§8  §7") + wrapped.get(i));
		}
		return list;
	}

	public static List<Location> getLocationsBetween(Location pos1, Location pos2, boolean skipFirst) {
		Validate.isTrue(pos1.getWorld() == pos2.getWorld(), "Worlds are different");
		World world = pos1.getWorld();
		
		
		int x1 = pos1.getBlockX();
		int y1 = pos1.getBlockY();
		int z1 = pos1.getBlockZ();
		int x2 = pos2.getBlockX();
		int y2 = pos2.getBlockY();
		int z2 = pos2.getBlockZ();
		int tipx = x1;
		int tipy = y1;
		int tipz = z1;
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int dz = Math.abs(z2 - z1);
		
		if (dx + dy + dz == 0) return skipFirst ? Collections.EMPTY_LIST : Arrays.asList(pos1);
		
		List<Location> locations = new ArrayList<>();
		
		int dMax = Math.max(Math.max(dx, dy), dz);
		if (dMax == dx) {
			for (int domstep = skipFirst ? 1 : 0; domstep <= dx; domstep++) {
				tipx = x1 + domstep * (x2 - x1 > 0 ? 1 : -1);
				tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dx) * (y2 - y1 > 0 ? 1 : -1));
				tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dx) * (z2 - z1 > 0 ? 1 : -1));
				
				locations.add(new Location(world, tipx, tipy, tipz));
			}
		}else if (dMax == dy) {
			for (int domstep = skipFirst ? 1 : 0; domstep <= dy; domstep++) {
				tipy = y1 + domstep * (y2 - y1 > 0 ? 1 : -1);
				tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dy) * (x2 - x1 > 0 ? 1 : -1));
				tipz = (int) Math.round(z1 + domstep * ((double) dz) / ((double) dy) * (z2 - z1 > 0 ? 1 : -1));
				
				locations.add(new Location(world, tipx, tipy, tipz));
			}
		}else /* if (dMax == dz) */ {
			for (int domstep = skipFirst ? 1 : 0; domstep <= dz; domstep++) {
				tipz = z1 + domstep * (z2 - z1 > 0 ? 1 : -1);
				tipy = (int) Math.round(y1 + domstep * ((double) dy) / ((double) dz) * (y2 - y1 > 0 ? 1 : -1));
				tipx = (int) Math.round(x1 + domstep * ((double) dx) / ((double) dz) * (x2 - x1 > 0 ? 1 : -1));
				
				locations.add(new Location(world, tipx, tipy, tipz));
			}
		}
		return locations;
	}
	
	public static String getBarsWithLoreLength(String itemName, List<String> lore, String inside) {
		int size = -1;
		if (!lore.isEmpty()) size = lore.stream().filter(x -> x != null).mapToInt(String::length).max().getAsInt();
		size = Math.max(size, ChatColor.stripColor(itemName).length() - 3);
		size -= ChatColor.stripColor(inside).length();
		size = Math.max(size, 4);
		String bar = "§m" + " ".repeat((int) Math.ceil(size / 2D));
		return "§e" + bar + "§e[ §6§l" + inside + "§e ]" + bar + "§r";
	}
	
}
