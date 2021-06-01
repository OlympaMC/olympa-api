package fr.olympa.api.spigot.frame;

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
import org.bukkit.plugin.Plugin;

import fr.olympa.api.spigot.config.CustomConfig;

@SuppressWarnings("deprecation")
public class ImageFrameManager implements Listener {

	public static final int MAP_WIDTH = 128;
	public static final int MAP_HEIGHT = 128;

	private String imagesDir;
	private String fileName;
	Plugin plugin;
	private Map<String, BufferedImage> images = new HashMap<>();
	private Map<UUID, PlacingCacheEntry> placing = new HashMap<>();
	private Map<Integer, ImageMap> maps = new HashMap<>();
	private List<Integer> sendList = new ArrayList<>();
	private FastSendTask sendTask;
	private List<ImageDownloadTask> downloadTasks;

	public String getFileName() {
		return fileName;
	}

	public ImageFrameManager(Plugin plugin, String mapsFile, String imageDir) {
		imagesDir = imageDir;
		this.plugin = plugin;
		//		if (!new File(plugin.getDataFolder(), imagesDir).exists())
		//			new File(plugin.getDataFolder(), imagesDir).mkdirs();

		int sendPerTicks = 0;
		int mapsPerSend = 0;

		fileName = mapsFile;
		loadMaps();
		new ImageMapCommand(plugin).register();
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
		sendTask = new FastSendTask(this, mapsPerSend);
		plugin.getServer().getPluginManager().registerEvents(sendTask, plugin);
		sendTask.runTaskTimer(plugin, sendPerTicks, sendPerTicks);
		downloadTasks = new ArrayList<>();
		new ImageDownloadCompleteNotifier(this).runTaskTimer(plugin, 20, 20);
	}

	public void appendDownloadTask(ImageDownloadTask task) {
		downloadTasks.add(task);
	}

	public List<ImageDownloadTask> getDownloadTasks() {
		return downloadTasks;
	}

	public List<Integer> getFastSendList() {
		return sendList;
	}

	private ItemStack getMapItem(String file, int x, int y, BufferedImage image, double scale) {
		ItemStack item = new ItemStack(Material.FILLED_MAP);

		for (Entry<Integer, ImageMap> entry : maps.entrySet())
			if (entry.getValue().isSimilar(file, x, y, scale)) {
				MapMeta meta = (MapMeta) item.getItemMeta();
				meta.setMapId(entry.getKey());
				item.setItemMeta(meta);
				return item;
			}

		MapView map = plugin.getServer().createMap(plugin.getServer().getWorlds().get(0));
		for (MapRenderer r : map.getRenderers())
			map.removeRenderer(r);

		map.addRenderer(new ImageMapRenderer(image, x, y, scale));

		MapMeta meta = (MapMeta) item.getItemMeta();
		meta.setMapId(map.getId());
		item.setItemMeta(meta);

		return item;
	}

	public BufferedImage loadImage(String file) {
		if (images.containsKey(file))
			return images.get(file);

		File f = new File(plugin.getDataFolder(), imagesDir + File.separatorChar + file);
		BufferedImage image = null;

		if (!f.exists())
			return null;

		try {
			image = ImageIO.read(f);
			images.put(file, image);
		} catch (IOException e) {
			plugin.getLogger().log(Level.SEVERE, "Error while trying to read image " + f.getName(), e);
		}

		return image;
	}

	private void loadMaps() {
		CustomConfig config = new CustomConfig(plugin, fileName);
		config.load();
		loadMaps(config);
	}

	public void loadMaps(FileConfiguration config) {
		//		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		Set<String> warnedFilenames = new HashSet<>();

		for (String key : config.getKeys(false)) {
			int id = Integer.parseInt(key);

			MapView map = plugin.getServer().getMap(id);

			if (map == null)
				continue;

			for (MapRenderer r : map.getRenderers())
				map.removeRenderer(r);

			String image = config.getString(key + ".image");
			int x = config.getInt(key + ".x");
			int y = config.getInt(key + ".y");
			boolean fastsend = config.getBoolean(key + ".fastsend", false);
			double scale = config.getDouble(key + ".scale", 1.0);

			BufferedImage bimage = loadImage(image);

			if (bimage == null) {
				if (!warnedFilenames.contains(image)) {
					warnedFilenames.add(image);
					plugin.getLogger().warning(() -> "Image file " + image + " not found, removing this map!");
				}
				continue;
			}

			if (fastsend)
				sendList.add(id);

			map.addRenderer(new ImageMapRenderer(loadImage(image), x, y, scale));
			maps.put(id, new ImageMap(image, x, y, fastsend, scale));
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
	public void onInteract(PlayerInteractEvent e) {
		if (!placing.containsKey(e.getPlayer().getUniqueId()))
			return;

		if (e.getAction() == Action.RIGHT_CLICK_AIR) {
			e.getPlayer().sendMessage("Placing cancelled");
			placing.remove(e.getPlayer().getUniqueId());
			return;
		}

		if (e.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (!placeImage(e.getClickedBlock(), e.getBlockFace(), placing.get(e.getPlayer().getUniqueId())))
			e.getPlayer().sendMessage(ChatColor.RED + "Can't place the image here!\nMake sure the area is large enough, unobstructed and without pre-existing hanging entities.");
		else
			saveMaps();

		e.setCancelled(true);
		placing.remove(e.getPlayer().getUniqueId());

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
			plugin.getLogger().severe("Someone tried to create an image with an invalid block facing");
			return false;
		}

		BufferedImage image = loadImage(cache.getImage());

		if (image == null) {
			plugin.getLogger().severe("Someone tried to create an image with an invalid file!");
			return false;
		}

		Block b = block.getRelative(face);

		int width = (int) Math.ceil((double) image.getWidth() / (double) MAP_WIDTH * cache.getScale() - 0.0001);
		int height = (int) Math.ceil((double) image.getHeight() / (double) MAP_HEIGHT * cache.getScale() - 0.0001);

		for (int x = 0; x < width; x++)
			for (int y = 0; y < height; y++) {
				if (!block.getRelative(x * xMod, -y, x * zMod).getType().isSolid())
					return false;

				if (block.getRelative(x * xMod - zMod, -y, x * zMod + xMod).getType().isSolid())
					return false;
			}

		try {
			for (int x = 0; x < width; x++)
				for (int y = 0; y < height; y++)
					setItemFrame(b.getRelative(x * xMod, -y, x * zMod), image, face, x * MAP_WIDTH, y * MAP_HEIGHT, cache);
		} catch (IllegalArgumentException e) {
			// God forgive me, but I actually HAVE to catch this...
			plugin.getLogger().info("Some error occured while placing the ItemFrames. This can for example happen when some existing ItemFrame/Hanging Entity is blocking.");
			plugin.getLogger().info("Unfortunatly this is caused be the way Minecraft/CraftBukkit handles the spawning of Entities.");
			return false;
		}

		return true;
	}

	public void reloadImage(String file) {
		images.remove(file);
		BufferedImage image = loadImage(file);

		if (image == null) {
			plugin.getLogger().warning(() -> "Failed to reload image: " + file);
			return;
		}

		maps.values().stream().filter(a -> a.getImage().equals(file)).forEach(imageMap -> {
			int id = ((MapMeta) getMapItem(file, imageMap.getX(), imageMap.getY(), image, imageMap.getScale()).getItemMeta()).getMapId();
			MapView map = plugin.getServer().getMap(id);

			for (MapRenderer renderer : map.getRenderers())
				if (renderer instanceof ImageMapRenderer)
					((ImageMapRenderer) renderer).recalculateInput(image, imageMap.getX(), imageMap.getY(), imageMap.getScale());

			sendTask.addToQueue(id);
		});
	}

	private void saveMaps() {
		File file = new File(plugin.getDataFolder(), "maps.yml");
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		for (String key : config.getKeys(false))
			config.set(key, null);

		for (Entry<Integer, ImageMap> e : maps.entrySet()) {
			config.set(e.getKey() + ".image", e.getValue().getImage());
			config.set(e.getKey() + ".x", e.getValue().getX());
			config.set(e.getKey() + ".y", e.getValue().getY());
			config.set(e.getKey() + ".fastsend", e.getValue().isFastSend());
			config.set(e.getKey() + ".scale", e.getValue().getScale());
		}

		try {
			config.save(file);
		} catch (IOException e1) {
			plugin.getLogger().log(Level.SEVERE, "Failed to save maps.yml!", e1);
		}
	}

	private void setItemFrame(Block bb, BufferedImage image, BlockFace face, int x, int y, PlacingCacheEntry cache) {
		ItemFrame i = null;

		i = bb.getWorld().spawn(bb.getLocation(), ItemFrame.class);

		i.setFacingDirection(face, false);

		ItemStack item = getMapItem(cache.getImage(), x, y, image, cache.getScale());
		i.setItem(item);

		int id = ((MapMeta) item.getItemMeta()).getMapId();

		if (cache.isFastSend() && !sendList.contains(id)) {
			sendList.add(id);
			sendTask.addToQueue(id);
		}

		maps.put(id, new ImageMap(cache.getImage(), x, y, sendList.contains(id), cache.getScale()));
	}

	public void startPlacing(Player p, String image, boolean fastsend, double scale) {
		placing.put(p.getUniqueId(), new PlacingCacheEntry(image, fastsend, scale));
	}
}
