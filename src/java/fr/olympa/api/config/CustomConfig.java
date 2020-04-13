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
import org.bukkit.plugin.Plugin;

import com.google.common.io.ByteStreams;

import fr.olympa.api.region.shapes.Cuboid;
import fr.olympa.api.utils.SpigotUtils;

public class CustomConfig extends YamlConfiguration {

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
		this.file = new File(plugin.getDataFolder(), this.filename);
		this.resource = plugin.getResource(filename);
	}

	public void eraseFile() {
		this.file.delete();
		try {
			this.file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Cuboid getCuboid(String path) {
		Location pos1 = this.getLocation(path + ".pos1");
		Location pos2 = this.getLocation(path + ".pos2");
		if (pos1 == null || pos2 == null) {
			return null;
		}
		return new Cuboid(pos1, pos2);
	}

	public File getFile() {
		return this.file;
	}

	@Override
	public Location getLocation(String path) {
		return SpigotUtils.convertStringToLocation(this.getString(path));
	}

	@Override
	public String getString(String path) {
		return SpigotUtils.color(super.getString(path));
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
		return this.resource != null;
	}

	public void load() {
		File file = this.getFile();
		File folder = file.getParentFile();
		if (!folder.exists()) {
			folder.mkdir();
		}
		try {
			if (!file.exists()) {
				file.createNewFile();
				if (this.resource != null) {
					ByteStreams.copy(this.resource, new FileOutputStream(file));
				}
				this.load(file);
			} else {
				this.load(file);
				CustomConfig resourceConfig = new CustomConfig();
				Double resourceConfigVersion = null;

				if (this.resource != null) {
					resourceConfig.load(new InputStreamReader(this.resource));
					resourceConfigVersion = resourceConfig.getVersion();

					Double version = this.getVersion();
					if (resourceConfigVersion != null && version != null) {
						if (resourceConfigVersion > version) {
							ByteStreams.copy(this.resource, new FileOutputStream(file));
							this.load(file);
							Bukkit.getLogger().log(Level.SEVERE, ChatColor.GREEN + "Config updated: " + this.filename);
						}
					}
				}

			}
		} catch (IOException | InvalidConfigurationException e) {
			Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED + "Unable to load config: " + this.filename);
			e.printStackTrace();
		}
	}

	public void save() {
		try {
			this.save(this.getFile());
		} catch (IOException e) {
			Bukkit.getLogger().log(Level.SEVERE, ChatColor.RED + "Unable to save config: " + this.filename);
			e.printStackTrace();
		}
	}

	public void saveIfNotExists() {
		if (!file.exists()) {
			plugin.saveResource(filename, true);
		}
	}

	public void set(String path, Cuboid cuboid) {
		this.set(path + ".pos1", cuboid.getPoint1());
		this.set(path + ".pos2", cuboid.getPoint2());
	}

	public void set(String path, Location location) {
		this.set(path, SpigotUtils.convertLocationToString(location));
	}
}
