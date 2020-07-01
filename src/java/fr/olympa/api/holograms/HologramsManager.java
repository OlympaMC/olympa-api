package fr.olympa.api.holograms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fr.olympa.api.utils.observable.Observable.Observer;
import fr.olympa.core.spigot.OlympaCore;

public class HologramsManager {

	public static NamespacedKey HOLOGRAM = new NamespacedKey(OlympaCore.getInstance(), "hologram");
	
	private Map<Integer, Hologram> holograms = new HashMap<>();
	private int lastInternalID = 0;

	private BiMap<Integer, Hologram> persistentHolograms = HashBiMap.create();
	private int lastID = 0;

	private File hologramsFile;
	private YamlConfiguration hologramsYaml;

	public HologramsManager(File hologramsFile) throws IOException {
		this.hologramsFile = hologramsFile;

		hologramsFile.getParentFile().mkdirs();
		hologramsFile.createNewFile();
		
		Bukkit.getScheduler().runTask(OlympaCore.getInstance(), () -> {
			hologramsYaml = YamlConfiguration.loadConfiguration(hologramsFile);

			for (String key : hologramsYaml.getKeys(false)) {
				int id = Integer.parseInt(key);
				lastID = Math.max(id + 1, lastID);
				Hologram hologram = (Hologram) hologramsYaml.get(key);
				persistentHolograms.put(id, hologram);
				Observer update = updateHologram(id, hologram);
				hologram.observe("manager_save", update);
			}
		});

		new HologramsCommand(this).register();
	}

	int addHologram(Hologram hologram) {
		int id = lastInternalID++;
		holograms.put(id, hologram);
		return id;
	}

	Hologram getHologram(int id) {
		return holograms.get(id);
	}
	
	void deleteHologram(Hologram hologram) {
		holograms.remove(hologram.internalID);
	}

	public int addPersistentHologram(Hologram hologram) {
		int id = lastID++;
		addHologram(hologram);
		persistentHolograms.put(id, hologram);
		Observer update = updateHologram(id, hologram);
		hologram.observe("manager_save", update);
		update.changed();
		return id;
	}

	private Observer updateHologram(int id, Hologram hologram) {
		return () -> {
			try {
				hologramsYaml.set(String.valueOf(id), hologram);
				hologramsYaml.save(hologramsFile);
			}catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	public boolean deletePersistentHologram(int id) {
		Hologram hologram = persistentHolograms.remove(id);
		if (hologram == null) return false;
		deleteHologram(hologram);
		updateHologram(id, null).changed();
		return true;
	}

	public Hologram getPersistentHologram(int id) {
		return persistentHolograms.get(id);
	}

	public Set<Entry<Integer, Hologram>> getPersistentHolograms() {
		return persistentHolograms.entrySet();
	}

	public void unload() {
		new ArrayList<>(holograms.values()).forEach(Hologram::remove);
	}

}
