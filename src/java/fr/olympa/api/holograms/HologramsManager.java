package fr.olympa.api.holograms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.bukkit.configuration.file.YamlConfiguration;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import fr.olympa.api.utils.observable.Observable.Observer;

public class HologramsManager {

	private List<Hologram> holograms = new ArrayList<>();

	private BiMap<Integer, Hologram> persistentHolograms = HashBiMap.create();
	private int lastID = 0;

	private File hologramsFile;
	private YamlConfiguration hologramsYaml;

	public HologramsManager(File hologramsFile) throws IOException {
		this.hologramsFile = hologramsFile;

		hologramsFile.createNewFile();
		hologramsYaml = YamlConfiguration.loadConfiguration(hologramsFile);

		for (String key : hologramsYaml.getKeys(false)) {
			int id = Integer.parseInt(key);
			lastID = Math.max(id + 1, lastID);
			Hologram hologram = (Hologram) hologramsYaml.get(key);
			holograms.add(hologram);
			persistentHolograms.put(id, hologram);
			Observer update = updateHologram(id, hologram);
			hologram.observe("manager_save", update);
		}

		new HologramsCommand(this).register();
	}

	public void addHologram(Hologram hologram) {
		holograms.add(hologram);
	}

	public void deleteHologram(Hologram hologram) {
		holograms.remove(hologram);
		hologram.destroy();
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

	public Hologram getHologram(int id) {
		return persistentHolograms.get(id);
	}

	public Set<Entry<Integer, Hologram>> getPersistentHolograms() {
		return persistentHolograms.entrySet();
	}

	public void unload() {
		holograms.forEach(Hologram::destroy);
		holograms.clear();
	}

}
