package fr.olympa.api.frame;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;

import javax.imageio.ImageIO;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.MapMeta;
import org.bukkit.map.MapRenderer;
import org.bukkit.map.MapView;
import org.bukkit.plugin.java.JavaPlugin;

public class ImageMaps implements Listener {
	public static final int MAP_WIDTH = 128;
	public static final int MAP_HEIGHT = 128;

	private final String IMAGES_DIR = "images";
	JavaPlugin plugin;
	private Map<String, BufferedImage> images = new HashMap<>();
	private Map<UUID, PlacingCacheEntry> placing = new HashMap<>();
	private Map<Integer, ImageMap> maps = new HashMap<>();
	private List<Integer> sendList = new ArrayList<>();
	private FastSendTask sendTask;
	private List<ImageDownloadTask> downloadTasks;

	public void appendDownloadTask(ImageDownloadTask task) {
		this.downloadTasks.add(task);
	}

	public void disable() {
		this.plugin.getServer().getScheduler().cancelTasks(this.plugin);
	}

	public void enable(JavaPlugin plugin) {
		this.plugin = plugin;
		if (!new File(plugin.getDataFolder(), this.IMAGES_DIR).exists()) {
			new File(plugin.getDataFolder(), this.IMAGES_DIR).mkdirs();
		}

		int sendPerTicks = 0;
		int mapsPerSend = 0;

		this.loadMaps();
		plugin.getCommand("imagemap").setExecutor(new ImageMapCommand(this));
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		this.sendTask = new FastSendTask(this, mapsPerSend);
		plugin.getServer().getPluginManager().registerEvents(this.sendTask, plugin);
		this.sendTask.runTaskTimer(plugin, sendPerTicks, sendPerTicks);
		this.downloadTasks = new ArrayList<>();
		new ImageDownloadCompleteNotifier(this).runTaskTimer(plugin, 20, 20);
	}

	public List<ImageDownloadTask> getDownloadTasks() {
		return this.downloadTasks;
	}

	public List<Integer> getFastSendList() {
		return this.sendList;
	}

	@SuppressWarnings("deprecation")
	private ItemStack getMapItem(String file, int x, int y, BufferedImage image, double scale) {
		ItemStack item = new ItemStack(Material.MAP);

		for (Entry<Integer, ImageMap> entry : this.maps.entrySet()) {
			if (entry.getValue().isSimilar(file, x, y, scale)) {
				MapMeta meta = (MapMeta) item.getItemMeta();
				meta.setMapId(entry.getKey());
				item.setItemMeta(meta);
				return item;
			}
		}

		MapView map = this.plugin.getServer().createMap(this.plugin.getServer().getWorlds().get(0));
		for (MapRenderer r : map.getRenderers()) {
			map.removeRenderer(r);
		}

		map.addRenderer(new ImageMapRenderer(image, x, y, scale));

		MapMeta meta = (MapMeta) item.getItemMeta();
		meta.setMapId(map.getId());
		item.setItemMeta(meta);

		return item;
	}

	public BufferedImage loadImage(String file) {
		if (this.images.containsKey(file)) {
			return this.images.get(file);
		}

		File f = new File(this.plugin.getDataFolder(), this.IMAGES_DIR + File.separatorChar + file);
		BufferedImage image = null;

		if (!f.exists()) {
			return null;
		}

		try {
			image = ImageIO.read(f);
			this.images.put(file, image);
		} catch (IOException e) {
			this.plugin.getLogger().log(Level.SEVERE, "Error while trying to read image " + f.getName(), e);
		}

		return image;
	}

	@SuppressWarnings("deprecation")
	private void loadMaps() {
		File file = new File(this.plugin.getDataFolder(), "maps.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		Set<String> warnedFilenames = new HashSet<>();

		for (String key : config.getKeys(false)) {
			int id = Integer.parseInt(key);

			MapView map = this.plugin.getServer().getMap(id);

			if (map == null) {
				continue;
			}

			for (MapRenderer r : map.getRenderers()) {
				map.removeRenderer(r);
			}

			String image = config.getString(key + ".image");
			int x = config.getInt(key + ".x");
			int y = config.getInt(key + ".y");
			boolean fastsend = config.getBoolean(key + ".fastsend", false);
			double scale = config.getDouble(key + ".scale", 1.0);

			BufferedImage bimage = this.loadImage(image);

			if (bimage == null) {
				if (!warnedFilenames.contains(image)) {
					warnedFilenames.add(image);
					this.plugin.getLogger().warning(() -> "Image file " + image + " not found, removing this map!");
				}
				continue;
			}

			if (fastsend) {
				this.sendList.add(id);
			}

			map.addRenderer(new ImageMapRenderer(this.loadImage(image), x, y, scale));
			this.maps.put(id, new ImageMap(image, x, y, fastsend, scale));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent e) {
		if (!this.placing.containsKey(e.getPlayer().getUniqueId())) {
			return;
		}

		if (e.getAction() == Action.RIGHT_CLICK_AIR) {
			e.getPlayer().sendMessage("Placing cancelled");
			this.placing.remove(e.getPlayer().getUniqueId());
			return;
		}

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK) {
			return;
		}

		if (!this.placeImage(e.getClickedBlock(), e.getBlockFace(), this.placing.get(e.getPlayer().getUniqueId()))) {
			e.getPlayer().sendMessage(ChatColor.RED + "Can't place the image here!\nMake sure the area is large enough, unobstructed and without pre-existing hanging entities.");
		} else {
			this.saveMaps();
		}

		e.setCancelled(true);
		this.placing.remove(e.getPlayer().getUniqueId());

	}

	public boolean placeImage(Block block, BlockFace face, PlacingCacheEntry cache) {
		int xMod = 0;
		int zMod = 0;

		switch (face) {
		case EAST:
			zMod = -1;
			break;
		case WEST:
			zMod = 1;
			break;
		case SOUTH:
			xMod = 1;
			break;
		case NORTH:
			xMod = -1;
			break;
		default:
			this.plugin.getLogger().severe("Someone tried to create an image with an invalid block facing");
			return false;
		}

		BufferedImage image = this.loadImage(cache.getImage());

		if (image == null) {
			this.plugin.getLogger().severe("Someone tried to create an image with an invalid file!");
			return false;
		}

		Block b = block.getRelative(face);

		int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH * cache.getScale() - 0.0001);
		int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT * cache.getScale() - 0.0001);

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (!block.getRelative(x * xMod, -y, x * zMod).getType().isSolid()) {
					return false;
				}

				if (block.getRelative(x * xMod - zMod, -y, x * zMod + xMod).getType().isSolid()) {
					return false;
				}
			}
		}

		try {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					this.setItemFrame(b.getRelative(x * xMod, -y, x * zMod), image, face, x * MAP_WIDTH, y * MAP_HEIGHT, cache);
				}
			}
		} catch (IllegalArgumentException e) {
			// God forgive me, but I actually HAVE to catch this...
			this.plugin.getLogger().info("Some error occured while placing the ItemFrames. This can for example happen when some existing ItemFrame/Hanging Entity is blocking.");
			this.plugin.getLogger().info("Unfortunatly this is caused be the way Minecraft/CraftBukkit handles the spawning of Entities.");
			return false;
		}

		return true;
	}

	@SuppressWarnings("deprecation")
	public void reloadImage(String file) {
		this.images.remove(file);
		BufferedImage image = this.loadImage(file);

		if (image == null) {
			this.plugin.getLogger().warning(() -> "Failed to reload image: " + file);
			return;
		}

		this.maps.values().stream().filter(a -> a.getImage().equals(file)).forEach(imageMap -> {
			int id = ((MapMeta) this.getMapItem(file, imageMap.getX(), imageMap.getY(), image, imageMap.getScale()).getItemMeta()).getMapId();
			MapView map = this.plugin.getServer().getMap(id);

			for (MapRenderer renderer : map.getRenderers()) {
				if (renderer instanceof ImageMapRenderer) {
					((ImageMapRenderer) renderer).recalculateInput(image, imageMap.getX(), imageMap.getY(), imageMap.getScale());
				}
			}

			this.sendTask.addToQueue(id);
		});
	}

	private void saveMaps() {
		File file = new File(this.plugin.getDataFolder(), "maps.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		for (String key : config.getKeys(false)) {
			config.set(key, null);
		}

		for (Entry<Integer, ImageMap> e : this.maps.entrySet()) {
			config.set(e.getKey() + ".image", e.getValue().getImage());
			config.set(e.getKey() + ".x", e.getValue().getX());
			config.set(e.getKey() + ".y", e.getValue().getY());
			config.set(e.getKey() + ".fastsend", e.getValue().isFastSend());
			config.set(e.getKey() + ".scale", e.getValue().getScale());
		}

		try {
			config.save(file);
		} catch (IOException e1) {
			this.plugin.getLogger().log(Level.SEVERE, "Failed to save maps.yml!", e1);
		}
	}

	private void setItemFrame(Block bb, BufferedImage image, BlockFace face, int x, int y, PlacingCacheEntry cache) {
		ItemFrame i = null;

		i = bb.getWorld().spawn(bb.getLocation(), ItemFrame.class);

		i.setFacingDirection(face, false);

		ItemStack item = this.getMapItem(cache.getImage(), x, y, image, cache.getScale());
		i.setItem(item);

		int id = ((MapMeta) item.getItemMeta()).getMapId();

		if (cache.isFastSend() && !this.sendList.contains(id)) {
			this.sendList.add(id);
			this.sendTask.addToQueue(id);
		}

		this.maps.put(id, new ImageMap(cache.getImage(), x, y, this.sendList.contains(id), cache.getScale()));
	}

	public void startPlacing(Player p, String image, boolean fastsend, double scale) {
		this.placing.put(p.getUniqueId(), new PlacingCacheEntry(image, fastsend, scale));
	}
}
