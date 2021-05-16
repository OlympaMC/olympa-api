package fr.olympa.api.bungee.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import com.google.common.io.ByteStreams;

import fr.olympa.api.utils.Utils;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class BungeeCustomConfig {

	private static final String fileExtension = ".yml";
	private static List<BungeeCustomConfig> configs = new ArrayList<>();

	public static List<BungeeCustomConfig> getConfigs() {
		return configs;
	}

	public static BungeeCustomConfig getConfig(String name) {
		return configs.stream().filter(c -> c.getName().equals(name) || c.getName().equals(name.substring(0, name.length() - 1 - fileExtension.length()))).findFirst()
				.orElse(configs.stream().filter(c -> c.fileName.equals(name)).findFirst().orElse(null));
	}

	private Plugin plugin;
	private String fileName;
	private Configuration configuration;

	public BungeeCustomConfig(Plugin plugin, String fileName) {
		this.plugin = plugin;
		if (!fileName.contains(fileExtension))
			fileName += fileExtension;
		this.fileName = fileName;
		configs.add(this);
	}

	public Double getVersion() {
		Object obj = this.getConfig().get("version");
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

	public Configuration getConfig() {
		return configuration;
	}

	public void reload() throws IOException {
		load();
		plugin.getProxy().getPluginManager().callEvent(new BungeeConfigReloadEvent(fileName, getConfig()));
	}

	public void loadSafe() {
		try {
			load();
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, ChatColor.RED + "Impossible de charger la config : " + fileName);
			e.printStackTrace();
		}
	}

	public void load() throws IOException {
		File folder = plugin.getDataFolder();
		if (!folder.exists())
			folder.mkdir();
		File configFile = new File(folder, fileName);
		if (!configFile.exists() || Utils.isEmptyFile(configFile)) {
			configFile.createNewFile();
			InputStream jarfile = plugin.getResourceAsStream(fileName);
			if (jarfile != null)
				ByteStreams.copy(jarfile, new FileOutputStream(configFile));
			configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
			ProxyServer.getInstance().getLogger().log(Level.INFO, ChatColor.GREEN + "nouvelle Config : " + fileName);
		} else {
			configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(configFile);
			Double version = getVersion();
			InputStream jarfile = plugin.getResourceAsStream(fileName);
			if (jarfile != null) {
				Configuration jarconfig = ConfigurationProvider.getProvider(YamlConfiguration.class).load(jarfile);
				Double jarVersion = jarconfig.getDouble("version");
				if (jarVersion != null && version != null)
					if (jarVersion > version) {
						configFile.renameTo(new File(folder, fileName + " V" + configuration.getDouble("version")));
						configFile = new File(folder, fileName);
						configFile.createNewFile();
						ByteStreams.copy(plugin.getResourceAsStream(fileName), new FileOutputStream(configFile));
						configuration = jarconfig;
						ProxyServer.getInstance().getLogger().log(Level.INFO, ChatColor.GREEN + "Config updated : " + fileName);
					}
			}
		}
	}

	public String getFileName() {
		return fileName;
	}

	public void saveSafe() {
		try {
			save();
		} catch (IOException e) {
			ProxyServer.getInstance().getLogger().log(Level.SEVERE, ChatColor.RED + "Impossible de sauvegarder la config : " + fileName);
			e.printStackTrace();
		}
	}

	public void save() throws IOException {
		ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, new File(plugin.getDataFolder(), fileName));
	}

	public String getName() {
		return plugin.getDescription().getName() + "/" + fileName;
	}

}
