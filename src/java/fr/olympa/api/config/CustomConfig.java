package fr.olympa.api.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteStreams;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.customevents.SpigotConfigReloadEvent;
import fr.olympa.api.lines.CyclingLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.plugin.OlympaAPIPlugin;
import fr.olympa.api.region.shapes.ChunkCuboid;
import fr.olympa.api.region.shapes.ChunkPolygon;
import fr.olympa.api.region.shapes.Cuboid;
import fr.olympa.api.region.shapes.Cylinder;
import fr.olympa.api.region.shapes.ExpandedCuboid;
import fr.olympa.api.region.shapes.Polygon;
import fr.olympa.api.region.shapes.WorldRegion;
import fr.olympa.api.utils.Utils;
import fr.olympa.api.utils.spigot.SpigotUtils;

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

	private String fileName;
	private Plugin plugin;

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

	public void eraseFile() {
		configFile.delete();
		try {
			configFile.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return configFile;
	}

	@Override
	public Location getLocation(String path) {
		return SpigotUtils.convertStringToLocation(this.getString(path));
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

	public void reload() throws FileNotFoundException, IOException, InvalidConfigurationException {
		loadUnSafe();
		((OlympaAPIPlugin) plugin).getTask().runTaskAsynchronously(() -> plugin.getServer().getPluginManager().callEvent(new SpigotConfigReloadEvent(this)));
	}

	public void loadUnSafe() throws FileNotFoundException, IOException, InvalidConfigurationException {
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
	}

	public void load() {
		try {
			loadUnSafe();
		} catch (IOException | InvalidConfigurationException e) {
			System.err.println("Unable to load config: " + fileName);
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
			System.err.println("Unable to save config: " + fileName);
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
