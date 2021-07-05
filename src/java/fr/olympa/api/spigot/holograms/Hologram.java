package fr.olympa.api.spigot.holograms;

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
import org.bukkit.craftbukkit.v1_16_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftArmorStand;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.api.common.observable.AbstractObservable;
import fr.olympa.api.spigot.lines.AbstractLine;
import fr.olympa.api.spigot.lines.FixedLine;
import fr.olympa.api.spigot.lines.LinesHolder;
import fr.olympa.api.spigot.utils.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EntityArmorStand;
import net.minecraft.server.v1_16_R3.EntityTypes;
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
	private final boolean packetHolo;
	
	private final boolean defaultVisibility;
	private final Set<Player> players = new HashSet<>();
	
	private boolean willSpawn = false;
	
	public Hologram(int id, Location bottom, boolean persistent, boolean defaultVisibility, AbstractLine<HologramLine>... lines) {
		this(id, bottom, persistent, defaultVisibility, false, lines);
	}
	
	public Hologram(int id, Location bottom, boolean persistent, boolean defaultVisibility, boolean packetHolo, AbstractLine<HologramLine>... lines) {
		setBottom(bottom);
		
		this.id = id;
		this.entityMetadata = new FixedMetadataValue(OlympaCore.getInstance(), id);
		this.persistent = persistent;
		this.packetHolo = packetHolo;
		this.defaultVisibility = defaultVisibility;

		for (AbstractLine<HologramLine> line : lines) 
			addLine(line);
		
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
	
	public boolean isPacketHolo() {
		return packetHolo;
	}
	
	public void setVisibility(Player p, boolean visibility) {
		if (visibility) {
			show(p);
		}else hide(p);
	}
	
	public void show(Player p) {
		/*if (defaultVisibility) {
			if (!players.remove(p)) 
				return;
		}else if (!players.add(p)) 
			return;*/
		if (!players.add(p))
			return;
		
		if (isNear(p)) 
			lines.forEach(line -> line.showTo(p));
	}
	
	public void forceShow(Player p) {
		players.add(p);
		lines.forEach(line -> line.showTo(p));
	}
	
	public void hide(Player p) {
		/*if (defaultVisibility) {
			if (!players.add(p)) 
				return;
		}else if (!players.remove(p)) 
			return;*/
		if (!players.remove(p))
			return;
		
		if (isNear(p)) 
			lines.forEach(line -> line.hideTo(p));
	}
	
	public void forceHide(Player p) {
		players.remove(p);
		lines.forEach(line -> line.hideTo(p));
	}
	
	public void show() {
		if (packetHolo)
			return;
		
		willSpawn = true;
		spawnEntities();
	}
	
	public void hide() {
		if (packetHolo)
			return;
		
		willSpawn = false;
		destroyEntities();
	}
	
	public boolean isHidden() {
		return !willSpawn && !packetHolo;
	}
	
	public boolean isVisibleTo(Player p) {
		//return defaultVisibility != players.contains(p);
		return defaultVisibility || players.contains(p);
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
		//lines.forEach(HologramLine::updatePosition);
		holoLine.spawnEntity();
		lines.forEach(HologramLine::updatePosition);
		update();
	}

	public void removeLine(AbstractLine<HologramLine> line) {
		for (int i = 0; i < lines.size(); i++) {
			HologramLine hline = lines.get(i);
			if (hline.line == line) {
				lines.remove(i);
				hline.destroyEntity();
				lines.forEach(HologramLine::updatePosition);
				update();
				break;
			}
		}
	}

	public void removeLine(int index) {
		if (lines.size() <= index)
			return;
		
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
	
	@Deprecated
	public List<HologramLine> getLines() {
		return lines;
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
		private EntityArmorStand entityNms;

		public HologramLine(AbstractLine<HologramLine> line) {
			this.line = line;
			
			if (isPacketHolo())
				spawnEntity();
		}
		
		void spawnEntity() {

			//System.out.println("Trying to spawn entity for text : " + line);

			if (isPacketHolo() && entityNms == null) {
				entityNms = new EntityArmorStand(EntityTypes.ARMOR_STAND, ((CraftWorld)getBottom().getWorld()).getHandle());
				entityNms.setLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), 0, 0);
				entityNms.setInvisible(true);
				entityNms.persistentInvisibility = true;
				
				entityNms.setInvulnerable(true);
				entityNms.setNoGravity(true);
				entityNms.setMarker(true);
				entityNms.setSmall(true);
				
				update(line, line.getValue(this));
				line.addHolder(this);
				//updatePosition();
				
				players.forEach(p -> show(p));
				
			}else if (!isPacketHolo() && entity == null) {
				
				if (!willSpawn)
					return;

				bottom.getWorld().loadChunk(bottom.getChunk());

				entity = getBottom().getWorld().spawn(getPosition(), ArmorStand.class, entity -> {
					entity.setGravity(false);
					entity.setMarker(true);
					entity.setSmall(true);
					entity.setVisible(false);
					entity.setInvulnerable(true);
					entity.setPersistent(false);
					entity.setMetadata("hologram", entityMetadata);
				});
				update(line, line.getValue(this));
				line.addHolder(this);
			}
		}

		private void destroyEntity() {
			if (entityNms != null) {
				players.forEach(p -> hideTo(p));
				entityNms = null;
				
			}else if (entity != null) {
				entity.remove();
				entity = null;
			}
			
			line.removeHolder(this);
		}
		
		public ArmorStand getEntity() {
			return entity;
		}
		
		public EntityArmorStand getNmsEntity() {
			return entityNms;
		}
		
		public AbstractLine<HologramLine> getLine(){
			return line;
		}
		
		public Location getPosition() {
			return bottom.clone().add(0, (lines.size() - lines.indexOf(this) - 1) * LINE_SPACING, 0);
		}
		 
		public void updatePosition() {
			if (entityNms != null) {
				players.forEach(p -> hideTo(p));
				entityNms.setLocation(getPosition().getX(), getPosition().getY(), getPosition().getZ(), 0, 0);
				//System.out.println("Set line " + line + " at pos " + bottom.clone().add(0, (lines.size() - lines.indexOf(this) - 1) * LINE_SPACING, 0));
				players.forEach(p -> showTo(p));
				
			}
			
			if (entity != null)
				entity.teleport(getPosition());
		}
		
		@Override
		public void update(AbstractLine<HologramLine> line, String newValue) {
			if (entityNms != null) {
				players.forEach(p -> hideTo(p));
				if (!"".equals(newValue)) {
					entityNms.setCustomNameVisible(true);
					entityNms.setCustomName(new ChatComponentText(newValue));
				}else
					entityNms.setCustomNameVisible(false);
				players.forEach(p -> showTo(p));
				
			}
			
			if (entity != null) {
				if (!"".equals(newValue)) {
					entity.setCustomNameVisible(true);
					entity.setCustomName(newValue);
				}else 
					entity.setCustomNameVisible(false);
			}
		}
		
		private void hideTo(Player p) {
			if (entityNms != null) {
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entityNms.getId()));
			}
			
			if (entity != null) 
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityDestroy(entity.getEntityId()));
		}
		
		private void showTo(Player p) {
			if (entityNms != null) {
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(entityNms));
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(entityNms.getId(), entityNms.getDataWatcher(), true));
				
			}
			
			if (entity != null) {
				EntityArmorStand nmsEntity = ((CraftArmorStand) entity).getHandle();
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutSpawnEntityLiving(nmsEntity));
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(nmsEntity.getId(), nmsEntity.getDataWatcher(), true));
			}
		}
		
		@Override
		public String getName() {
			return "hologramme #" + id;
		}
	}

}
