package fr.olympa.api.spigot.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteStreams;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.spigot.customevents.SpigotConfigReloadEvent;
import fr.olympa.api.spigot.lines.CyclingLine;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.region.shapes.ChunkCuboid;
import fr.olympa.api.spigot.region.shapes.ChunkPolygon;
import fr.olympa.api.spigot.region.shapes.Cuboid;
import fr.olympa.api.spigot.region.shapes.Cylinder;
import fr.olympa.api.spigot.region.shapes.ExpandedCuboid;
import fr.olympa.api.spigot.region.shapes.Polygon;
import fr.olympa.api.spigot.region.shapes.WorldRegion;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;

public class CustomConfig extends YamlConfiguration {

	private static List<CustomConfig> configs = new ArrayList<>();

	public static List<CustomConfig> getConfigs() {
		return configs;
	}

	public static CustomConfig getConfig(String name) {
		return configs.stream().filter(c -> c.getName().equals(name)).findFirst().orElse(configs.stream().filter(c -> c.fileName.equals(name)).findFirst().orElse(null));
	}

	{
		ConfigurationSerialization.registerClass(Cuboid.class);
		ConfigurationSerialization.registerClass(ExpandedCuboid.class);
		ConfigurationSerialization.registerClass(ChunkCuboid.class);
		ConfigurationSerialization.registerClass(Polygon.class);
		ConfigurationSerialization.registerClass(ChunkPolygon.class);
		ConfigurationSerialization.registerClass(Cylinder.class);
		ConfigurationSerialization.registerClass(WorldRegion.class);

		ConfigurationSerialization.registerClass(FixedLine.class);
		ConfigurationSerialization.registerClass(CyclingLine.class);
	}

	private File configFile;

	private boolean isLoaded = false;
	private String fileName;
	private Plugin plugin;
	private Map<String, Consumer<CustomConfig>> tasks = new HashMap<>();

	public String getFileName() {
		return fileName;
	}

	public CustomConfig() {
		super();
	}

	public CustomConfig(Plugin plugin, String filename) {
		this.plugin = plugin;
		if (!filename.toLowerCase().endsWith(".yml"))
			filename += ".yml";
		fileName = filename;
		configFile = new File(plugin.getDataFolder(), fileName);
		configs.add(this);
	}

	public boolean addTask(String name, Consumer<CustomConfig> consumer) {
		if (isLoaded)
			consumer.accept(this);
		boolean isTaskDidntExist = tasks.put(name, consumer) == null;
		if (!isTaskDidntExist)
			new IllegalAccessError("Config Task " + name + " already exist. It was replaced but this won't be happened").printStackTrace();
		return isTaskDidntExist;
	}

	public boolean eraseFile() {
		try {
			Files.delete(configFile.toPath());
			return configFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public File getFile() {
		return configFile;
	}

	@Override
	public Location getLocation(String path) {
		return SpigotUtils.convertStringToLocation(this.getString(path));
	}

	public Material getMaterial(String path) {
		return Material.valueOf(this.getString(path));
	}

	@Override
	public String getString(String path) {
		return ColorUtils.color(super.getString(path));
	}

	public Double getVersion() {
		Object obj = this.get("version");
		if (obj instanceof Number)
			return ((Number) obj).doubleValue();
		else if (obj != null)
			try {
				return Double.valueOf(obj.toString());
			} catch (NumberFormatException e) {
				return null;
			}
		return null;
	}

	public void reload() throws IOException, InvalidConfigurationException {
		loadUnSafe();
		((OlympaAPIPlugin) plugin).getTask().runTaskAsynchronously(() -> plugin.getServer().getPluginManager().callEvent(new SpigotConfigReloadEvent(this)));
	}

	public void loadUnSafe() throws IOException, InvalidConfigurationException {
		File folder = configFile.getParentFile();
		if (!folder.exists())
			folder.mkdirs();
		InputStream resource = getRessource();
		if (!configFile.exists() || Utils.isEmptyFile(configFile)) {
			configFile.createNewFile();
			if (resource != null)
				ByteStreams.copy(resource, new FileOutputStream(configFile));
			this.load(configFile);
		} else {
			this.load(configFile);
			CustomConfig resourceConfig = new CustomConfig();
			Double resourceConfigVersion = null;

			if (resource != null) {
				resourceConfig.load(new InputStreamReader(resource));
				resourceConfigVersion = resourceConfig.getVersion();

				Double version = getVersion();
				if (resourceConfigVersion != null && version != null)
					if (resourceConfigVersion > version) {
						configFile.renameTo(new File(folder, configFile.getName() + " V" + version));
						configFile = new File(folder, fileName);
						configFile.createNewFile();
						ByteStreams.copy(getRessource(), new FileOutputStream(configFile));
						this.load(configFile);
						Bukkit.getLogger().log(Level.INFO, ChatColor.GREEN + "Config updated: " + fileName);
					}
			}
		}
		isLoaded = true;
		tasks.values().forEach(task -> task.accept(this));
	}

	public void load() {
		try {
			loadUnSafe();
		} catch (IOException | InvalidConfigurationException e) {
			e.initCause(new Throwable("Unable to load config: " + fileName));
			e.printStackTrace();
		}
	}

	public void saveUnSafe() throws IOException {
		this.save(configFile);
	}

	public void save() {
		try {
			saveUnSafe();
		} catch (IOException e) {
			e.initCause(new Throwable("Unable to save config: " + fileName));
			e.printStackTrace();
		}
	}

	public void saveIfNotExists() {
		if (!configFile.exists())
			plugin.saveResource(fileName, true);
	}

	public void set(String path, Location location) {
		this.set(path, SpigotUtils.convertLocationToString(location));
	}

	public void set(String path, Material material) {
		this.set(path, material.name());
	}

	public InputStream getRessource() {
		return plugin.getResource(fileName);
	}

	public boolean hasResource() {
		return getRessource() != null;
	}

	@Override
	public String getName() {
		return plugin.getDescription().getName() + "/" + fileName;
	}
}
