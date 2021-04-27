package fr.olympa.api.utils.machine;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

import fr.olympa.api.utils.Utils;

public class DebugPlugin {
	String name;
	String version;
	List<String> authors;
	boolean enabled;
	@Nullable
	String website;
	@Nullable
	Boolean dependNotFound;
	@Nullable
	Boolean softDependNotFound;
	String lastModifiedTime;
	boolean hasConfig;

	public DebugPlugin(Plugin plugin) {
		PluginDescriptionFile desc = plugin.getDescription();
		name = plugin.getName();
		version = desc.getVersion();
		authors = desc.getAuthors();
		website = desc.getWebsite();
		enabled = plugin.isEnabled();
		if (!enabled) {
			dependNotFound = Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).noneMatch(p -> desc.getDepend().stream().anyMatch(s2 -> p.getName().equalsIgnoreCase(s2)));
			softDependNotFound = Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).noneMatch(p -> desc.getSoftDepend().stream().anyMatch(s2 -> p.getName().equalsIgnoreCase(s2)));
		}
		try {
			File file = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			lastModifiedTime = Utils.tsToShortDur(attr.lastModifiedTime().toMillis() / 1000L);
		} catch (Exception | NoClassDefFoundError e) {
			e.printStackTrace();
		}
		hasConfig = plugin.getConfig() != null;
	}

	public String getName() {
		return name;
	}

	public String getVersion() {
		return version;
	}

	public List<String> getAuthors() {
		return authors;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public boolean hasWebsite() {
		return website != null;
	}

	public String getWebsite() {
		return website;
	}

	public Boolean getDependNotFound() {
		return dependNotFound;
	}

	public Boolean getSoftDependNotFound() {
		return softDependNotFound;
	}

	public String getLastModifiedTime() {
		return lastModifiedTime;
	}
}