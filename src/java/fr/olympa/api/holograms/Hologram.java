package fr.olympa.api.holograms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.entity.ArmorStand;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.lines.LinesHolder;
import fr.olympa.api.utils.observable.AbstractObservable;
import fr.olympa.api.utils.spigot.SpigotUtils;

public class Hologram extends AbstractObservable implements LinesHolder<Hologram>, ConfigurationSerializable {

	private static final double LINE_SPACING = 0.3;

	private final List<Line> lines = new ArrayList<>();
	private Location bottom;

	public Hologram(Location bottom, AbstractLine<Hologram>... lines) {
		this.bottom = bottom.clone();
		this.bottom.setPitch(0);
		this.bottom.setYaw(0);

		for (AbstractLine<Hologram> line : lines) {
			addLine(line);
		}
	}

	public Location getBottom() {
		return bottom;
	}

	public void addLine(AbstractLine<Hologram> line) {
		lines.add(new Line(line));
		lines.forEach(Line::updatePosition);
		update();
	}

	public void removeLine(AbstractLine<Hologram> line) {
		for (int i = 0; i < lines.size(); i++) {
			Line hline = lines.get(i);
			if (hline.line == line) {
				hline.destroy();
				lines.remove(i);
				lines.forEach(Line::updatePosition);
				update();
				break;
			}
		}
	}

	public void removeLine(int index) {
		lines.remove(index).destroy();
		lines.forEach(Line::updatePosition);
		update();
	}

	@Override
	public void update(AbstractLine<Hologram> line) {
		for (Line hline : lines) {
			if (hline.line == line) {
				hline.updateText();
				break;
			}
		}
	}

	public void move(Location newBottom) {
		this.bottom = newBottom.clone();
		lines.forEach(Line::updatePosition);
	}

	@Override
	public String toString() {
		return lines.stream().map(x -> x.line.getValue(this)).collect(Collectors.joining("|"));
	}

	public void destroy() {
		clearObservers();
		lines.forEach(Line::destroy);
		lines.clear();
	}

	@Override
	public Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		map.put("bottom", SpigotUtils.convertLocationToString(getBottom()));
		map.put("lines", lines.stream().map(x -> {
			AbstractLine<Hologram> line = x.line;
			if (line instanceof ConfigurationSerializable) return line;
			return new FixedLine<>("§c" + line.getClass().getSimpleName() + " cannot be serialized");
		}).collect(Collectors.toList()));
		return map;
	}

	public static Hologram deserialize(Map<String, Object> map) {
		return new Hologram(SpigotUtils.convertStringToLocation((String) map.get("bottom")), ((List<AbstractLine<Hologram>>) map.get("lines")).toArray(AbstractLine[]::new));
	}

	class Line {
		private final AbstractLine<Hologram> line;
		private final ArmorStand entity;

		public Line(AbstractLine<Hologram> line) {
			this.line = line;
			this.entity = getBottom().getWorld().spawn(getBottom(), ArmorStand.class);
			entity.setGravity(false);
			entity.setMarker(true);
			entity.setSmall(true);
			entity.setVisible(false);
			entity.setInvulnerable(true);
			line.addHolder(Hologram.this);
			updateText();
		}

		public void updatePosition() {
			entity.teleport(getBottom().clone().add(0, (lines.size() - lines.indexOf(this) - 1) * LINE_SPACING, 0));
		}

		public void updateText() {
			String value = line.getValue(Hologram.this);
			if (!"".equals(value)) {
				entity.setCustomNameVisible(true);
				entity.setCustomName(value);
			}else entity.setCustomNameVisible(false);
		}

		public void destroy() {
			line.removeHolder(Hologram.this);
			entity.remove();
		}
	}

}
