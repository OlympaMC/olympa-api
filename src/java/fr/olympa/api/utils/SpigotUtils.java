package fr.olympa.api.utils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.provider.AccountProvider;

public class SpigotUtils {

	public static Location addYToLocation(final Location location, final float y) {
		return new Location(location.getWorld(), location.getX(), location.getY() + 1, location.getZ(), location.getYaw(), location.getPitch());
	}

	public static int clearPlayer(final Player player) {
		final PlayerInventory inventory = player.getInventory();
		final int size = (int) (Arrays.stream(inventory.getContents()).filter(item -> item != null).count() + Arrays.stream(inventory.getArmorContents()).filter(item -> item != null).count());
		inventory.clear();
		inventory.setArmorContents(new ItemStack[inventory.getArmorContents().length]);
		return size;
	}

	public static List<String> color(final List<String> l) {
		return l.stream().map(s -> SpigotUtils.color(s)).collect(Collectors.toList());
	}

	public static String color(final String s) {
		return s != null ? ChatColor.translateAlternateColorCodes('&', s) : "";
	}

	public static String connectScreen(final String s) {
		return color("\n&e&m-------------------------------------------\n\n&e[&6Olympa&e]\n\n" + s + "\n\n&e&m-------------------------------------------");
	}

	public static String convertLocationToString(final Location loc) {
		final String world = loc.getWorld().getName();
		final double x = loc.getX();
		final double y = loc.getY();
		final double z = loc.getZ();
		if ((int) loc.getPitch() != 0) {
			final int pitch = (int) loc.getPitch();
			final int yaw = (int) loc.getYaw();
			return world + " " + x + " " + y + " " + z + " " + pitch + " " + yaw;
		}
		return world + " " + x + " " + y + " " + z;
	}

	public static String convertBlockLocationToString(final Location loc) {
		return loc.getBlockX() + " " + loc.getBlockY() + " " + loc.getBlockZ();
	}

	public static Location convertStringToLocation(final String loc) {
		if (loc != null) {
			final String[] coords = loc.split(" ");
			final World w = Bukkit.getWorld(coords[0]);
			final double x = Double.parseDouble(coords[1]);
			final double y = Double.parseDouble(coords[2]);
			final double z = Double.parseDouble(coords[3]);
			if (coords.length == 6) {
				final float pitch = Float.parseFloat(coords[4]);
				final float yaw = Float.parseFloat(coords[5]);
				return new Location(w, x, y, z, pitch, yaw);
			}
			return new Location(w, x, y, z);
		}
		return null;
	}

	public static List<Location> getBlockAround(final Location location, final int raduis) {
		final List<Location> locations = new ArrayList<>();
		for (int x = raduis; x >= -raduis; x--) {
			for (int y = raduis; y >= -raduis; y--) {
				for (int z = raduis; z >= -raduis; z--) {
					locations.add(location.getBlock().getRelative(x, y, z).getLocation());
				}
			}
		}
		return locations;
	}

	public static Cuboid getCuboid(ConfigurationSection section, String key) {
		if (section != null) {
			Location loc1 = convertStringToLocation(section.getString(key + ".pos1"));
			Location loc2 = convertStringToLocation(section.getString(key + ".pos2"));
			Cuboid cuboid = new Cuboid(loc1, loc2);
			return cuboid;
		}
		return null;
	}

	public static Location getFirstBlockUnderPlayer(final Player player) {
		Location location = player.getLocation();
		do {
			if (location.getBlockY() == 0) {
				return null;
			}
			location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		} while (!location.getBlock().getType().isOccluding());
		return location;
	}

	public static ChatColor getIntervalChatColor(final int i, final int min, final int max) {
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

	public static short getIntervalGlassPaneColor(final int i, final int min, final int max) {
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

	public static Player getNearestPlayer(final Player checkNear) {
		Player nearest = null;
		for (final Player p : checkNear.getWorld().getPlayers()) {
			if (nearest == null) {
				nearest = p;
			} else if (p.getLocation().distance(checkNear.getLocation()) < nearest.getLocation().distance(checkNear.getLocation())) {
				nearest = p;
			}
		}
		return nearest;
	}

	public static boolean hasEnoughPlace(final Inventory inventory, final ItemStack... items) {
		final Inventory inventory2 = Bukkit.createInventory(null, inventory.getSize());
		inventory2.setContents(inventory.getContents());
		int amount1 = 0;
		for (final ItemStack item : inventory2.getContents()) {
			if (item != null) {
				amount1 += item.getAmount();
			}
		}

		int amount2 = 0;
		for (final ItemStack item : items) {
			amount2 += item.getAmount();
		}

		final int amount3 = amount1 + amount2;
		inventory2.addItem(items);

		int amount4 = 0;
		for (final ItemStack item : inventory2.getContents()) {
			if (item != null) {
				amount4 += item.getAmount();
			}
		}

		if (amount4 == amount3) {
			return true;
		}

		return false;
	}

	public static boolean isIn(final Location loc, final Location playerLoc) {
		return playerLoc.getWorld() == loc.getWorld() && loc.getBlockX() == playerLoc.getBlockX() && loc.getBlockY() == playerLoc.getBlockY() && loc.getBlockZ() == playerLoc.getBlockZ();
	}

	/*
	 * public static EmeraldPlayer getPlayer(final Player player) { final
	 * EmeraldPlayer emeraldPlayer = EmeraldPlayers.getPlayer(player.getUniqueId());
	 * if(emeraldPlayer != null) { return emeraldPlayer; } return
	 * MySQL.getPlayer(player.getUniqueId()); }
	 */

	public static boolean isOnGround(final Player player) {
		Location location = player.getLocation();
		location = new Location(location.getWorld(), location.getBlockX(), location.getBlockY() - 1, location.getBlockZ());
		return location.getBlock().getType() == Material.AIR;
	}

	public static boolean isSameLocation(final Location location1, final Location location2) {
		return location1.getBlockX() == location2.getBlockX() && location1.getBlockY() == location2.getBlockY() && location1.getBlockZ() == location2.getBlockZ();
	}

	public static boolean isSamePlayer(final Player player, final Player target) {
		return player.getUniqueId().equals(target.getUniqueId());
	}

	public static boolean playerisIn(final Player player, final Location location) {
		final Location playerLocation = player.getLocation();
		return playerLocation.getBlockX() == location.getBlockX() && (playerLocation.getBlockY() == location.getBlockY() || playerLocation.getBlockY() + 1 == location.getBlockY()) && playerLocation.getBlockZ() == location.getBlockZ();
	}

}
