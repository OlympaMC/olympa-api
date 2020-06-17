package fr.olympa.api.holograms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.LinesHolder;

public class Hologram implements LinesHolder<Hologram> {

	public List<Line> lines = new ArrayList<>();
	private Location location;

	public Hologram(Location location, AbstractLine<Hologram>... lines) {
		this.location = location;

		for (AbstractLine<Hologram> line : lines) {
			addLine(line);
		}
	}

	public void addLine(AbstractLine<Hologram> line) {
		lines.add(new Line(line));
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

	public void destroy() {
		lines.forEach(Line::destroy);
		lines.clear();
	}

	class Line {
		private final AbstractLine<Hologram> line;
		private final ArmorStand entity;

		public Line(AbstractLine<Hologram> line) {
			this.line = line;
			this.entity = location.getWorld().spawn(location.add(0, lines.size() * 0.3, 0), ArmorStand.class);
			entity.setGravity(false);
			entity.setMarker(true);
			entity.setSmall(true);
			entity.setCustomNameVisible(true);
			entity.setVisible(false);
			entity.setInvulnerable(true);
			line.addHolder(Hologram.this);
			updateText();
		}

		public void updateText() {
			entity.setCustomName(line.getValue(Hologram.this));
		}

		public void destroy() {
			line.removeHolder(Hologram.this);
			entity.remove();
		}
	}

}
