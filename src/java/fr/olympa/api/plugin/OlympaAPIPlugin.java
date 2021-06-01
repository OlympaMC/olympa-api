package fr.olympa.api.plugin;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;

import javax.annotation.Nullable;

import org.bukkit.plugin.java.JavaPlugin;

import fr.olympa.api.chat.ColorUtils;
import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.holograms.HologramsManager;
import fr.olympa.api.task.OlympaTask;
import fr.olympa.api.task.TaskManager;
import fr.olympa.api.utils.Utils;

public abstract class OlympaAPIPlugin extends JavaPlugin implements OlympaPluginInterface {

	protected final OlympaTask task;
	protected CustomConfig config;
	protected HologramsManager hologramsManager;
	@Nullable
	private Long lastModifiedTime;

	public HologramsManager getHologramsManager() {
		return hologramsManager;
	}

	public void setHologramsManager(HologramsManager hologramsManager) {
		this.hologramsManager = hologramsManager;
	}

	public OlympaAPIPlugin() {
		task = new TaskManager(this);
	}

	@Override
	public CustomConfig getConfig() {
		return config;
	}

	@Override
	public String getPrefixConsole() {
		return "&f[&6" + getDescription().getName() + "&f] &e";
	}

	public long getLastModifiedLong() {
		if (lastModifiedTime != null)
			return lastModifiedTime;
		try {
			File file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
			BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
			lastModifiedTime = attr.lastModifiedTime().toMillis() / 1000L;
			return lastModifiedTime;
		} catch (Exception | NoClassDefFoundError e) {
			e.printStackTrace();
		}
		return 0;
	}

	public String getLastModifiedTime() {
		return Utils.tsToShortDur(getLastModifiedLong());
	}

	@Override
	public OlympaTask getTask() {
		return task;
	}

	@Override
	public void onEnable() {
		super.onEnable();
		config = new CustomConfig(this, "config");
		if (config.hasResource() || config.getFile().exists()) {
			config.load();
			config.saveIfNotExists();
		} else
			config = null;
	}

	@Override
	public void onDisable() {
		if (hologramsManager != null)
			hologramsManager.unload();
		super.onDisable();
	}

	@Override
	public void sendMessage(String message, Object... args) {
		getServer().getConsoleSender().sendMessage(ColorUtils.color(String.format(getPrefixConsole() + message, args)));
	}

}
