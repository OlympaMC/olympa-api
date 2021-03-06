package fr.olympa.api.spigot.holograms;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;

import fr.olympa.api.common.module.OlympaModule;
import fr.olympa.api.common.module.OlympaModule.ModuleApi;
import fr.olympa.api.common.module.SpigotModule;
import fr.olympa.api.common.observable.Observable.Observer;
import fr.olympa.api.common.plugin.OlympaAPIPlugin;
import fr.olympa.api.spigot.command.OlympaCommand;
import fr.olympa.api.spigot.config.CustomConfig;
import fr.olympa.api.spigot.holograms.Hologram.HologramLine;
import fr.olympa.api.spigot.lines.AbstractLine;
import fr.olympa.api.spigot.region.Point2D;
import fr.olympa.core.spigot.OlympaCore;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntityLiving;

public class HologramsManager implements Listener, ModuleApi<OlympaAPIPlugin> {

	@Override
	public boolean disable(OlympaAPIPlugin plugin) {
		unload();
		hologramsYaml = null;
		return true;
	}

	public boolean reload(CustomConfig config) {
		if (!isEnabled())
			return false;
		unload();
		load(config);
		return true;
	}

	@Override
	public boolean enable(OlympaAPIPlugin plugin) {
		hologramsYaml = new CustomConfig(plugin, hologramsFile.getName());
		hologramsYaml.addTask("module_holo", customConfig -> reload(customConfig));
		hologramsYaml.load();
		load(hologramsYaml);
		return true;
	}

	@Override
	public boolean isEnabled() {
		return hologramsYaml != null;
	}

	@Override
	public boolean setToPlugin(OlympaAPIPlugin plugin) {
		plugin.setHologramsManager(this);
		return true;
	}

	public void unload() {
		holograms.values().forEach(Hologram::destroy);
		holograms.clear();
	}

	public void load(CustomConfig config) {
		OlympaAPIPlugin plugin = config.getPlugin();
		Map<Integer, Map<String, Object>> toDeserialize = new HashMap<>();
		for (String key : hologramsYaml.getKeys(false)) {
			int id = Integer.parseInt(key);
			lastID = Math.max(id + 1, lastID);
			toDeserialize.put(id, hologramsYaml.getConfigurationSection(key).getValues(false));
		}
		plugin.getTask().runTask(() -> {
			toDeserialize.forEach((id, map) -> {
				Hologram hologram = Hologram.deserialize(map, id, true);
				holograms.put(id, hologram);
				Observer update = updateHologram(id, hologram);
				hologram.observe("manager_save", update);
				if (!hologram.getBottom().isChunkLoaded()) {
					chunksUnloaded.compute(Point2D.chunkPointFromLocation(hologram.getBottom()), (point, list) -> {
						if (list == null) list = new ArrayList<>();
						list.add(id);
						return list;
					});
				}
			});
		});
		entityType.setAccessible(true);
		entityID.setAccessible(true);
	}

	private final Map<Point2D, List<Integer>> chunksUnloaded = new HashMap<>();

	Map<Integer, Hologram> holograms = new ConcurrentHashMap<>();
	private int lastID = 0;

	private final File hologramsFile;
	private CustomConfig hologramsYaml;

	private final Field entityType = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("c");
	private final Field entityID = PacketPlayOutSpawnEntityLiving.class.getDeclaredField("a");
	private final int armorStandEntityType = IRegistry.ENTITY_TYPE.a(EntityTypes.ARMOR_STAND);

	private HoloAccessControl accessController = (sender, holo, action) -> true;
	private boolean createDefaultPacketHolo = false;

	public HologramsManager(OlympaAPIPlugin pl, File hologramsFile) throws NoSuchFieldException {
		this.hologramsFile = hologramsFile;
		OlympaModule<HologramsManager, Listener, OlympaAPIPlugin, OlympaCommand> module = new SpigotModule<>(pl, "holograms_" + pl.getName(), plugin -> this)
				.listener(this.getClass()).cmd(HologramsCommand.class);
		try {
			module.enableModule();
		} catch (Exception e) {
			e.printStackTrace();
		}
		module.registerModule();
		//		hologramsFile.getParentFile().mkdirs();
		//		hologramsFile.createNewFile();
	}

	public Hologram createHologram(Location location, boolean persistent, boolean defaultVisibility, boolean onlyPackets, AbstractLine<HologramLine>... lines) {
		Validate.notNull(location, "Hologram location cannot be null");
		int id = lastID++;
		Hologram hologram = new Hologram(id, location, persistent, defaultVisibility, onlyPackets, lines);
		holograms.put(id, hologram);
		if (persistent) {
			Observer update = updateHologram(id, hologram);
			hologram.observe("manager_save", update);
			try {
				update.changed();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return hologram;
	}

	public Hologram createHologram(Location location, boolean persistent, boolean defaultVisibility, AbstractLine<HologramLine>... lines) {
		return createHologram(location, persistent, defaultVisibility, false, lines);
	}

	/**
	 * Only use this if holo has been created with createHologram originally
	 * @param id
	 * @param holo
	 * @return
	 */
	@Deprecated
	public boolean registerHologram(int id, Hologram holo) {
		if (holograms.containsKey(id))
			return false;

		holograms.put(id, holo);

		if (holo.isPersistent()) {
			Observer update = updateHologram(id, holo);
			holo.observe("manager_save", update);
			try {
				update.changed();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return true;
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
			try {
				updateHologram(hologram.getID(), null).changed();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return true;
	}

	public boolean deleteHologram(int id) {
		Hologram hologram = holograms.remove(id);
		if (hologram == null)
			return false;
		hologram.destroy();
		if (hologram.isPersistent())
			try {
				updateHologram(hologram.getID(), null).changed();
			} catch (Exception e) {
				e.printStackTrace();
			}
		return true;
	}

	@EventHandler
	public void onChunkUnload(ChunkUnloadEvent e) {
		List<Integer> holoIDs = null;
		for (Entity entity : e.getChunk().getEntities())
			if (!entity.isDead() && entity.hasMetadata("hologram")) {
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

	@EventHandler //respawn entity if killed by any other process than normal holo removing
	public void onRemoveEntity(EntityRemoveFromWorldEvent e) {
		if (e.getEntityType() != EntityType.ARMOR_STAND)
			return;

		if (e.getEntity().hasMetadata("hologram")) {
			Hologram hologram = holograms.get(e.getEntity().getMetadata("hologram").get(0).asInt());
			if (hologram != null) {
				HologramLine line = hologram.getFromArmorStand(e.getEntity().getEntityId());
				if (line != null)
					Bukkit.getScheduler().runTaskLater(OlympaCore.getInstance(), () -> line.spawnEntity(), 2);
			}
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		((CraftPlayer) e.getPlayer()).getHandle().playerConnection.networkManager.channel.pipeline().addBefore("packet_handler", "holograms_visibilty", new ChannelDuplexHandler() {

			@Override
			public void write(ChannelHandlerContext ctx, Object msg, io.netty.channel.ChannelPromise promise) throws Exception {
				try {
					if (msg instanceof PacketPlayOutSpawnEntityLiving) {
						PacketPlayOutSpawnEntityLiving packet = (PacketPlayOutSpawnEntityLiving) msg;
						if (entityType.getInt(packet) == armorStandEntityType) {
							int id = entityID.getInt(packet);
							for (Hologram holo : holograms.values())
								if (holo.containsArmorStand(id))
									if (!holo.isVisibleTo(e.getPlayer()))
										return; // return = cancel le packet
						}
					}
					super.write(ctx, msg, promise);
				} catch (Exception ex) {
					OlympaCore.getInstance().sendMessage("??cUne erreur est survenue lors de la gestion des entit??s hologrammes.");
					ex.printStackTrace();
				}
			}
		});
	}

	/**
	 * Permet de d??finir avec plus de finesse quelles actions les joueurs
	 * sont autoris??s ?? ex??cuter sur un hologramme donn??
	 * @param accessControler
	 */
	public void setHoloControlSupplier(HoloAccessControl accessControler) {
		accessController = accessControler;
	}

	public boolean hasAccessTo(CommandSender sender, Hologram holo, HoloActionType action) {
		return accessController.apply(sender, holo, action);
	}

	/**
	 * Si vrai, alors tous les holos cr????s par les commandes seront
	 * temporaires et enti??rement en packets
	 * @param b (default false)
	 */
	public void setTempHoloCreationMode(boolean b) {
		createDefaultPacketHolo = b;
	}

	public boolean hasTempHoloCreationMode() {
		return createDefaultPacketHolo;
	}

	@FunctionalInterface
	public interface HoloAccessControl {
		boolean apply(CommandSender sender, Hologram holo, HoloActionType action);
	}

	public enum HoloActionType {
		COMMAND,
		CREATE_PREPROCESS,
		CREATED,
		REMOVE,
		EDIT_ADDLINE,
		EDIT_OTHER,
		MOVE,
		TELEPORT,
		VISIBILITY
	}

}
