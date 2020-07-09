package fr.olympa.api.holograms;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.utils.Point2D;
import fr.olympa.api.utils.observable.Observable.Observer;
import fr.olympa.core.spigot.OlympaCore;

public class HologramsManager implements Listener {
	
	private Map<Point2D, List<Integer>> chunksUnloaded = new HashMap<>();
	
	Map<Integer, Hologram> holograms = new HashMap<>();
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
				Hologram hologram = Hologram.deserialize(hologramsYaml.getConfigurationSection(key).getValues(false), id, true);
				holograms.put(id, hologram);
				Observer update = updateHologram(id, hologram);
				hologram.observe("manager_save", update);
			}
		});
		
		new HologramsCommand(this).register();
	}

	public Hologram createHologram(Location location, boolean persistent, AbstractLine<HologramLine>... lines) {
		int id = lastID++;
		Hologram hologram = new Hologram(id, location, persistent, lines);
		holograms.put(id, hologram);
		if (persistent) {
			Observer update = updateHologram(id, hologram);
			hologram.observe("manager_save", update);
			update.changed();
		}
		return hologram;
	}

	Hologram getHologram(int id) {
		return holograms.get(id);
	}

	private Observer updateHologram(int id, Hologram hologram) {
		return () -> {
			try {
				hologramsYaml.set(String.valueOf(id), hologram.serialize());
				hologramsYaml.save(hologramsFile);
			}catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	public boolean deleteHologram(Hologram hologram) {
		if (holograms.remove(hologram.getID()) == null) return false;
		hologram.destroy();
		updateHologram(hologram.getID(), null).changed();
		return true;
	}

	public void unload() {
		new ArrayList<>(holograms.values()).forEach(this::deleteHologram);
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		List<Integer> holoIDs = null;
		for (Entity entity : e.getChunk().getEntities()) {
			if (entity.hasMetadata("hologram")) {
				Hologram hologram = holograms.get(entity.getMetadata("hologram").get(0).asInt());
				hologram.destroyEntities();
				if (holoIDs == null) holoIDs = new ArrayList<>();
				holoIDs.add(hologram.getID());
			}
		}
		if (holoIDs != null) chunksUnloaded.put(new Point2D(e.getChunk()), holoIDs);
	}
	
	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		Point2D point = new Point2D(e.getChunk());
		List<Integer> list = chunksUnloaded.remove(point);
		if (list != null) {
			for (Integer holoID : list) {
				Hologram hologram = holograms.get(holoID);
				if (hologram != null) hologram.spawnEntities();
			}
		}
	}
	
}
