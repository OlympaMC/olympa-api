package fr.olympa.api.common.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;

import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginDescription;

public class PluginInfoBungee extends PluginInfoAdvanced {

	public PluginInfoBungee(Plugin plugin) {
		PluginDescription desc = plugin.getDescription();
		name = desc.getName();
		version = desc.getVersion();
		SuperVersion supVersion = new SuperVersion(desc.getVersion());
		superVersion = supVersion;

		if (desc.getAuthor() == null)
			authors = new ArrayList<>();
		else
			authors = Arrays.asList(desc.getAuthor().split(" *, *"));
		description = desc.getDescription();
		//		apiVersion = desc.getAPIVersion();
		//		provides = desc.getProvides();
		provides = new ArrayList<>();
		//		contributors = desc.getContributors();
		contributors = new ArrayList<>();
		//		website = desc.getWebsite();
		enabled = true;
		//		enabled = plugin.isEnabled();
		//		if (!enabled) {
		//			dependNotFound = Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).noneMatch(p -> desc.getDepend().stream().anyMatch(s2 -> p.getName().equalsIgnoreCase(s2)));
		//			softDependNotFound = Arrays.stream(plugin.getServer().getPluginManager().getPlugins()).noneMatch(p -> desc.getSoftDepend().stream().anyMatch(s2 -> p.getName().equalsIgnoreCase(s2)));
		//		}
		try {
			File file = new File(plugin.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			lastModifiedTime = attr.lastModifiedTime().toMillis() / 1000L;
		} catch (Exception | NoClassDefFoundError e) {
			e.printStackTrace();
		}
		//		hasConfig = plugin.getConfig() != null;
	}
}
