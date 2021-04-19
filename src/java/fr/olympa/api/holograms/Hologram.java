package fr.olympa.api.holograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.LinesHolder;
import fr.olympa.api.utils.observable.AbstractObservable;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntityLiving;

public class Hologram extends AbstractObservable {

	private static final double LINE_SPACING = 0.3;
	private static final int VISIBILITY_DISTANCE_SQUARED = 64 * 64;

	private final List<HologramLine> lines = new ArrayList<>();
	private Location bottom;

	private final int id;
	private final FixedMetadataValue entityMetadata;
	private final boolean persistent;
	
	private final boolean defaultVisibility;
	private final Set<Player> players = new HashSet<>();
	
	private boolean willSpawn = false;
	
	Hologram(int id, Location bottom, boolean persistent, boolean defaultVisibility, AbstractLine<HologramLine>... lines) {
		setBottom(bottom);
		
		this.id = id;
		this.entityMetadata = new FixedMetadataValue(OlympaCore.getInstance(), id);
		this.persistent = persistent;
		this.defaultVisibility = defaultVisibility;

		for (AbstractLine<HologramLine> line : lines) {
			addLine(line);
		}
		
		willSpawn = true;
		if (Bukkit.isPrimaryThread()) {
			spawnEntities();
		}else Bukkit.getScheduler().runTask(OlympaCore.getInstance(), this::spawnEntities);
	}

	public Location getBottom() {
		return bottom;
	}
	
	private void setBottom(Location newBottom) {
		bottom = newBottom.clone();
		bottom.setPitch(0);
		bottom.setYaw(0);
	}
	
	public int getID() {
		return id;
	}
	
	public boolean isPersistent() {
		return persistent;
	}
	
	public void setVisibility(Player p, boolean visibility) {
		if (visibility) {
			show(p);
		}else hide(p);
	}
	
	public void show(Player p) {
		if (defaultVisibility) {
			if (!players.remove(p)) return;
		}else if (!players.add(p)) return;
		if (isNear(p)) lines.forEach(line -> line.showTo(p));
	}
	
	public void hide(Player p) {
		if (defaultVisibility) {
			if (!players.add(p)) return;
		}else if (!players.remove(p)) return;
		if (isNear(p)) lines.forEach(line -> line.hideTo(p));
	}
	
	public void show() {
		willSpawn = true;
		spawnEntities();
	}
	
	public void hide() {
		willSpawn = false;
		destroyEntities();
	}
	
	public boolean isHidden() {
		return !willSpawn;
	}
	
	public boolean isVisibleTo(Player p) {
		return defaultVisibility != players.contains(p);
	}
	
	public boolean containsArmorStand(int entityID) {
		for (HologramLine line : lines) {
			if (line.entity != null && (line.entity.getEntityId() == entityID)) return true;
		}
		return false;
	}
	
	public HologramLine getFromArmorStand(int entityID) {
		for (HologramLine line : lines) {
			if (line.entity != null && (line.entity.getEntityId() == entityID)) return line;
		}
		return null;
	}
	
	public void addLine(AbstractLine<HologramLine> line) {
		insertLine(line, lines.size());
	}

	public void insertLine(AbstractLine<HologramLine> line, int index) {
		HologramLine holoLine = new HologramLine(line);
		lines.add(index, holoLine);
		lines.forEach(HologramLine::updatePosition);
		holoLine.spawnEntity();
		update();
	}

	public void removeLine(AbstractLine<HologramLine> line) {
		for (int i = 0; i < lines.size(); i++) {
			HologramLine hline = lines.get(i);
			if (hline.line == line) {
				hline.destroyEntity();
				lines.remove(i);
				lines.forEach(HologramLine::updatePosition);
				update();
				break;
			}
		}
	}

	public void removeLine(int index) {
		lines.remove(index).destroyEntity();
		lines.forEach(HologramLine::updatePosition);
		update();
	}
	
	public void setLine(AbstractLine<HologramLine> line, int index) {
		if (index >= lines.size()) {
			addLine(line);
		}else {
			removeLine(index);
			insertLine(line, index);
		}
	}

	public void move(Location newBottom) {
		setBottom(newBottom);
		lines.forEach(HologramLine::updatePosition);
		update();
	}
	
	private boolean isNear(Player player) {
		return player.isOnline() && player.getWorld().equals(bottom.getWorld()) && player.getLocation().distanceSquared(getBottom()) < VISIBILITY_DISTANCE_SQUARED;
	}

	@Override
	public String toString() {
		return lines.stream().map(x -> x.line.getValue(x)).collect(Collectors.joining("§7§l | §r"));
	}

	public void remove() {
		OlympaCore.getInstance().getHologramsManager().deleteHologram(this);
	}
	
	void destroyEntities() {
		lines.forEach(HologramLine::destroyEntity);
	}
	
	void spawnEntities() {
		lines.forEach(HologramLine::spawnEntity);
	}
	
	void destroy() {
		clearObservers();
		destroyEntities();
		lines.clear();
	}

	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("bottom", SpigotUtils.convertLocationToString(bottom));
		map.put("lines", lines.stream().map(x -> {
			AbstractLine<HologramLine> line = x.line;
			if (line instanceof ConfigurationSerializable) return line;
			return new FixedLine<>("§c" + line.getClass().getSimpleName() + " cannot be serialized");
		}).collect(Collectors.toList()));
		return map;
	}

	static Hologram deserialize(Map<String, Object> map, int id, boolean persistent) {
		return new Hologram(id, SpigotUtils.convertStringToLocation((String) map.get("bottom")), persistent, true, ((List<AbstractLine<HologramLine>>) map.get("lines")).toArray(AbstractLine[]::new));
	}
	
	public class HologramLine implements LinesHolder<HologramLine> {
		private final AbstractLine<HologramLine> line;
		private ArmorStand entity;

		public HologramLine(AbstractLine<HologramLine> line) {
			this.line = line;
		}
		
		void spawnEntity() {
			if (entity != null) return;
			if (!willSpawn || !bottom.getChunk().isLoaded()) return;
			entity = getBottom().getWorld().spawn(getPosition(), ArmorStand.class);
			entity.setGravity(false);
			entity.setMarker(true);
			entity.setSmall(true);
			entity.setVisible(false);
			entity.setInvulnerable(true);
			entity.getEntityId();
			entity.setPersistent(false);
			entity.setMetadata("hologram", entityMetadata);
			update(line, line.getValue(this));
			line.addHolder(this);
		}

		private void destroyEntity() {
			if (entity != null) {
				entity.remove();
				entity = null;
			}
			line.removeHolder(this);
		}
		
		public Location getPosition() {
			return bottom.clone().add(0, (lines.size() - lines.indexOf(this) - 1) * LINE_SPACING, 0);
		}
		 
		public void updatePosition() {
			if (entity == null) return;
			entity.teleport(getPosition());
		}
		
		@Override
		public void update(AbstractLine<HologramLine> line, String newValue) {
			if (entity == null) return;
			if (!"".equals(newValue)) {
				entity.setCustomNameVisible(true);
				entity.setCustomName(newValue);
			}else entity.setCustomNameVisible(false);
		}
		
		private void hideTo(Player p) {
			if (entity == null) return;
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entity.getEntityId()));
		}
		
		private void showTo(Player p) {
			if (entity == null) return;
			EntityArmorStand nmsEntity = ((CraftArmorStand) entity).getHandle();
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(nmsEntity));
			((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true));
		}
		
		@Override
		public String getName() {
			return "hologramme #" + id;
		}
	}

}
