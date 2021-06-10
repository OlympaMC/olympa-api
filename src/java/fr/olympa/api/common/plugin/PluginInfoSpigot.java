package fr.olympa.api.common.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;

public class PluginInfoSpigot extends PluginInfoAdvanced {

	public PluginInfoSpigot(Plugin plugin) {
		PluginDescriptionFile desc = plugin.getDescription();
		name = plugin.getName();
		version = desc.getVersion();
		SuperVersion supVersion = new SuperVersion(desc.getVersion());
		superVersion = supVersion;
		authors = desc.getAuthors();
		description = desc.getDescription();
		apiVersion = desc.getAPIVersion();
		provides = desc.getProvides();
		contributors = desc.getContributors();
		website = desc.getWebsite();
		enabled = plugin.isEnabled();
		if (!enabled) {
			dependNotFound = Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).noneMatch(p -> desc.getDepend().stream().anyMatch(s2 -> p.getName().equalsIgnoreCase(s2)));
			softDependNotFound = Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).noneMatch(p -> desc.getSoftDepend().stream().anyMatch(s2 -> p.getName().equalsIgnoreCase(s2)));
		}
		try {
			File file = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			lastModifiedTime = attr.lastModifiedTime().toMillis() / 1000L;
		} catch (Exception | NoClassDefFoundError e) {
			e.printStackTrace();
		}
		hasConfig = plugin.getConfig() != null;
	}

}