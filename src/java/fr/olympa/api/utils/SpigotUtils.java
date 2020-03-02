package fr.olympa.api.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.util.Vector;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;
import fr.olympa.api.region.Cuboid;
import fr.olympa.api.region.Region;
import net.md_5.bungee.api.ChatColor;

public class SpigotUtils {

	public static Location addYToLocation(Location location, float y) {
		return new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ(), location.getYaw(), location.getPitch());
	}

	public static int clearPlayer(Player player) {
		PlayerInventory inventory = player.getInventory();
		int size = (int) (Arrays.stream(inventory.getContents()).filter(item -> item != null).count() + Arrays.stream(inventory.getArmorContents()).filter(item -> item != null).count());
		inventory.clear();
		inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
		return size;
	}

	public static List<String> color(List<String> l) {
		return l.stream().map(s -> SpigotUtils.color(s)).collect(Collectors.toList());
	}

	public static String color(String s) {
		return s != null ? ChatColor.translateAlternateColorCodes('&', s) : "";
	}

	public static String connectScreen(String s) {
		return SpigotUtils.color("\n&e&m-------------------------------------------\n\n&e[&6Olympa&e]\n\n" + s + "\n\n&e&m-------------------------------------------");
	}

	public static String convertBlockLocationToString(Location loc) {
		return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
	}

	public static String convertLocationToString(Location loc) {
		String world = loc.getWorld().getName();
		double x = loc.getX();
		double y = loc.getY();
		double z = loc.getZ();
		if ((int) loc.getPitch() != 0) {
			int pitch = (int) loc.getPitch();
			int yaw = (int) loc.getYaw();
			return world + " " + x + " " + y + " " + z + " " + pitch + " " + yaw;
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
				float pitch = Float.parseFloat(coords[4]);
				float yaw = Float.parseFloat(coords[5]);
				return new Location(w, x, y, z, pitch, yaw);
			}
			return new Location(w, x, y, z);
		}
		return null;
	}

	public static List<Location> getBlockAround(Location location, int raduis) {
		List<Location> locations = new ArrayList<>();
		for (int x = raduis; x >= -raduis; x--) {
			for (int y = raduis; y >= -raduis; y--) {
				for (int z = raduis; z >= -raduis; z--) {
					locations.add(location.getBlock().getRelative(x, y, z).getLocation());
				}
			}
		}
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
			if (location.getBlockY() == 0) {
				return null;
			}
			location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		} while (!location.getBlock().getType().isOccluding());
		return location;
	}

	public static ChatColor getIntervalChatColor(int i, int min, int max) {
		if (i == 0) {
			return ChatColor.GRAY;
		}
		if (i < min) {
			return ChatColor.GREEN;
		}
		if (i < max) {
			return ChatColor.GOLD;
		}
		return ChatColor.RED;
	}

	public static short getIntervalGlassPaneColor(int i, int min, int max) {
		if (i == 0) {
			return 0;
		}
		if (i < min) {
			return 5;
		}
		if (i < max) {
			return 1;
		}
		return 14;
	}

	public static String getName(UUID playerUniqueId) {
		Player player = Bukkit.getPlayer(playerUniqueId);
		if (player != null) {
			return player.getName();
		}
		try {
			OlympaPlayer olympaPlayer = new AccountProvider(playerUniqueId).get();
			if (olympaPlayer != null) {
				return olympaPlayer.getName();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return "";
	}

	public static Player getNearestPlayer(Player checkNear) {
		Player nearest = null;
		for (Player p : checkNear.getWorld().getPlayers()) {
			if (nearest == null) {
				nearest = p;
			} else if (p.getLocation().distance(checkNear.getLocation()) < nearest.getLocation().distance(checkNear.getLocation())) {
				nearest = p;
			}
		}
		return nearest;
	}

	public static boolean hasEnoughPlace(Inventory inventory, ItemStack... items) {
		Inventory inventory2 = Bukkit.createInventory(null, inventory.getSize());
		inventory2.setContents(inventory.getContents());
		int amount1 = 0;
		for (ItemStack item : inventory2.getContents()) {
			if (item != null) {
				amount1 += item.getAmount();
			}
		}

		int amount2 = 0;
		for (ItemStack item : items) {
			amount2 += item.getAmount();
		}

		int amount3 = amount1 + amount2;
		inventory2.addItem(items);

		int amount4 = 0;
		for (ItemStack item : inventory2.getContents()) {
			if (item != null) {
				amount4 += item.getAmount();
			}
		}

		if (amount4 == amount3) {
			return true;
		}

		return false;
	}

	public static boolean isIn(Location loc, Location playerLoc) {
		return playerLoc.getWorld() == loc.getWorld() && loc.getBlockX() == playerLoc.getBlockX() && loc.getBlockY() == playerLoc.getBlockY() && loc.getBlockZ() == playerLoc.getBlockZ();
	}

	public static boolean isOnGround(Player player) {
		Location location = player.getLocation();
		location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		return location.getBlock().getType() == Material.AIR;
	}

	public static boolean isSameLocation(Location location1, Location location2) {
		return location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY() && location1.getBlockZ() == location2.getBlockZ();
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
		if (player.getWorld() != target.getWorld()) return 'x';
		Location source = player.getLocation();
		Vector inBetween = target.clone().subtract(source).toVector();
		Vector lookVec = source.getDirection();

		double angleDir = (Math.atan2(inBetween.getZ(), inBetween.getX()));
		double angleLook = (Math.atan2(lookVec.getZ(), lookVec.getX()));

		double angle = ((angleDir - angleLook + pi2) % pi2) / 2;
		return arrows[(int) Math.floor(angle / piOver16)];
	}

}
