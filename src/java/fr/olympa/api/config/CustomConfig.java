package fr.olympa.api.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteStreams;

import fr.olympa.api.region.shapes.ChunkCuboid;
import fr.olympa.api.region.shapes.ChunkPolygon;
import fr.olympa.api.region.shapes.Cuboid;
import fr.olympa.api.region.shapes.ExpandedCuboid;
import fr.olympa.api.region.shapes.Polygon;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.SpigotUtils;

public class CustomConfig extends YamlConfiguration {

	{
		ConfigurationSerialization.registerClass(Cuboid.class);
		ConfigurationSerialization.registerClass(ExpandedCuboid.class);
		ConfigurationSerialization.registerClass(ChunkCuboid.class);
		ConfigurationSerialization.registerClass(Polygon.class);
		ConfigurationSerialization.registerClass(ChunkPolygon.class);
	}

	private InputStream resource;
	private File file;

	private String filename;
	private Plugin plugin;

	public CustomConfig() {
		super();
	}

	public CustomConfig(Plugin plugin, String filename) {
		this.plugin = plugin;
		if (!filename.toLowerCase().endsWith(".yml")) {
			filename += ".yml";
		}
		this.filename = filename;
		file = new File(plugin.getDataFolder(), this.filename);
		resource = plugin.getResource(filename);
	}

	public void eraseFile() {
		file.delete();
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public File getFile() {
		return file;
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
		if (obj instanceof Number) {
			return ((Number) obj).doubleValue();
		} else if (obj != null) {
			try {
				return Double.valueOf(obj.toString());
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return null;
	}

	public boolean hasResource() {
		return resource != null;
	}

	public void load() {
		File file = getFile();
		File folder = file.getParentFile();
		if (!folder.exists()) {
			folder.mkdir();
		}
		try {
			if (!file.exists()) {
				file.createNewFile();
				if (resource != null) {
					ByteStreams.copy(resource, new FileOutputStream(file));
				}
				this.load(file);
			} else {
				this.load(file);
				CustomConfig resourceConfig = new CustomConfig();
				Double resourceConfigVersion = null;

				if (resource != null) {
					resourceConfig.load(new InputStreamReader(resource));
					resourceConfigVersion = resourceConfig.getVersion();

					Double version = getVersion();
					if (resourceConfigVersion != null && version != null) {
						if (resourceConfigVersion > version) {
							ByteStreams.copy(resource, new FileOutputStream(file));
							this.load(file);
							Bukkit.getLogger().log(Level.SEVERE, ChatColor.GREEN + "Config updated: " + filename);
						}
					}
				}

			}
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED + "Unable to load config: " + filename);
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			this.save(getFile());
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED + "Unable to save config: " + filename);
			e.printStackTrace();
		}
	}

	public void saveIfNotExists() {
		if (!file.exists()) {
			plugin.saveResource(filename, true);
		}
	}

	public void set(String path, Location location) {
		this.set(path, SpigotUtils.convertLocationToString(location));
	}
}
