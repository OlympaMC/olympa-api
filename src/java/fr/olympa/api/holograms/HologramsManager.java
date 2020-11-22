package fr.olympa.api.holograms;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import fr.olympa.api.config.CustomConfig;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.utils.Point2D;
import fr.olympa.api.utils.observable.Observable.Observer;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_15_R1.EntityTypes;
import net.minecraft.server.v1_15_R1.PacketPlayOutSpawnEntity;

public class HologramsManager implements Listener {

	private final Map<Point2D, List<Integer>> chunksUnloaded = new HashMap<>();

	Map<Integer, Hologram> holograms = new HashMap<>();
	private int lastID = 0;

	private final File hologramsFile;
	private final CustomConfig hologramsYaml;
	
	private final Field entityType = PacketPlayOutSpawnEntity.class.getDeclaredField("k");
	private final Field entityID = PacketPlayOutSpawnEntity.class.getDeclaredField("a");

	public File getFile() {
		return hologramsFile;
	}

	public HologramsManager(File hologramsFile) throws IOException, ReflectiveOperationException {
		this.hologramsFile = hologramsFile;

		//		hologramsFile.getParentFile().mkdirs();
		//		hologramsFile.createNewFile();

		Map<Integer, Map<String, Object>> toDeserialize = new HashMap<>();
		
		hologramsYaml = new CustomConfig(OlympaCore.getInstance(), hologramsFile.getName());
		hologramsYaml.load();
		for (String key : hologramsYaml.getKeys(false)) {
			int id = Integer.parseInt(key);
			lastID = Math.max(id + 1, lastID);
			toDeserialize.put(id, hologramsYaml.getConfigurationSection(key).getValues(false));
		}
		
		Bukkit.getScheduler().runTask(OlympaCore.getInstance(), () -> {
			toDeserialize.forEach((id, map) -> {
				Hologram hologram = Hologram.deserialize(map, id, true);
				holograms.put(id, hologram);
				Observer update = updateHologram(id, hologram);
				hologram.observe("manager_save", update);
			});
		});
		
		entityType.setAccessible(true);
		entityID.setAccessible(true);
	}
	
	public Hologram createHologram(Location location, boolean persistent, boolean defaultVisibility, AbstractLine<HologramLine>... lines) {
		int id = lastID++;
		Hologram hologram = new Hologram(id, location, persistent, defaultVisibility, lines);
		holograms.put(id, hologram);
		if (persistent) {
			Observer update = updateHologram(id, hologram);
			hologram.observe("manager_save", update);
			update.changed();
		}
		return hologram;
	}

	public Hologram getHologram(int id) {
		return holograms.get(id);
	}

	private Observer updateHologram(int id, Hologram hologram) {
		return () -> {
			try {
				hologramsYaml.set(String.valueOf(id), hologram == null ? null : hologram.serialize());
				hologramsYaml.save(hologramsFile);
			} catch (Exception e) {
				e.printStackTrace();
			}
		};
	}

	public boolean deleteHologram(Hologram hologram) {
		if (holograms.remove(hologram.getID()) == null)
			return false;
		hologram.destroy();
		if (hologram.isPersistent())
			updateHologram(hologram.getID(), null).changed();
		return true;
	}

	public boolean deleteHologram(int id) {
		Hologram hologram = holograms.remove(id);
		if (hologram == null)
			return false;
		hologram.destroy();
		if (hologram.isPersistent())
			updateHologram(hologram.getID(), null).changed();
		return true;
	}

	public void unload() {
		holograms.values().forEach(Hologram::destroy);
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		List<Integer> holoIDs = null;
		for (Entity entity : e.getChunk().getEntities())
			if (entity.hasMetadata("hologram")) {
				Hologram hologram = holograms.get(entity.getMetadata("hologram").get(0).asInt());
				hologram.destroyEntities();
				if (holoIDs == null)
					holoIDs = new ArrayList<>();
				holoIDs.add(hologram.getID());
			}
		if (holoIDs != null)
			chunksUnloaded.put(new Point2D(e.getChunk()), holoIDs);
	}

	@EventHandler
	public void onChunkLoad(ChunkLoadEvent e) {
		Point2D point = new Point2D(e.getChunk());
		List<Integer> list = chunksUnloaded.remove(point);
		if (list != null)
			for (Integer holoID : list) {
				Hologram hologram = holograms.get(holoID);
				if (hologram != null)
					hologram.spawnEntities();
			}
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		((CraftPlayer) e.getPlayer()).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", "holograms_visibilty", new ChannelDuplexHandler() {
			
			@Override
			public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception {
				if (msg instanceof PacketPlayOutSpawnEntity) {
					PacketPlayOutSpawnEntity packet = (PacketPlayOutSpawnEntity) msg;
					if (entityType.get(packet) == EntityTypes.ARMOR_STAND) {
						int id = entityID.getInt(packet);
						for (Hologram holo : holograms.values()) {
							if (holo.containsArmorStand(id)) {
								if (!holo.isVisibleTo(e.getPlayer())) {
									System.out.println("HologramsManager.onJoin(...).new ChannelDuplexHandler() {...}.write() cancelled hologram");
									return; // return = cancel le packet
								}
							}
						}
					}
				}
				super.write(ctx, msg, promise);
			}
		});
	}

}
