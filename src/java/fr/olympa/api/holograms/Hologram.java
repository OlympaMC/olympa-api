package fr.olympa.api.holograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;
import org.bukkit.metadata.FixedMetadataValue;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.LinesHolder;
import fr.olympa.api.utils.observable.AbstractObservable;
import fr.olympa.api.utils.spigot.SpigotUtils;
import fr.olympa.core.spigot.OlympaCore;

public class Hologram extends AbstractObservable {

	private static final double LINE_SPACING = 0.3;

	private final List<HologramLine> lines = new ArrayList<>();
	private Location bottom;

	private final int id;
	private final FixedMetadataValue entityMetadata;
	private final boolean persistent;
	
	Hologram(int id, Location bottom, boolean persistent, AbstractLine<HologramLine>... lines) {
		setBottom(bottom);
		
		this.id = id;
		this.entityMetadata = new FixedMetadataValue(OlympaCore.getInstance(), id);
		this.persistent = persistent;

		for (AbstractLine<HologramLine> line : lines) {
			addLine(line);
		}
		
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
	
	public boolean containsArmorStand(ArmorStand stand) {
		for (HologramLine line : lines) {
			if (line.entity.equals(stand)) return true;
		}
		return false;
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
		return new Hologram(id, SpigotUtils.convertStringToLocation((String) map.get("bottom")), persistent, ((List<AbstractLine<HologramLine>>) map.get("lines")).toArray(AbstractLine[]::new));
	}

	public class HologramLine implements LinesHolder<HologramLine> {
		private final AbstractLine<HologramLine> line;
		private ArmorStand entity;

		public HologramLine(AbstractLine<HologramLine> line) {
			this.line = line;
		}
		
		private void spawnEntity() {
			if (entity != null) return;
			entity = getBottom().getWorld().spawn(getPosition(), ArmorStand.class);
			entity.setGravity(false);
			entity.setMarker(true);
			entity.setSmall(true);
			entity.setVisible(false);
			entity.setInvulnerable(true);
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
	}

}
