package fr.olympa.api.holograms;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.LinesHolder;

public class Hologram implements LinesHolder<Hologram> {

	private static final double LINE_SPACING = 0.3;

	public List<Line> lines = new ArrayList<>();
	private Location bottom;

	public Hologram(Location bottom, AbstractLine<Hologram>... lines) {
		this.bottom = bottom;

		for (AbstractLine<Hologram> line : lines) {
			addLine(line);
		}
	}

	public void addLine(AbstractLine<Hologram> line) {
		lines.forEach(x -> x.entity.teleport(x.entity.getLocation().add(0, LINE_SPACING, 0)));
		lines.add(new Line(line));
	}

	public void removeLine(AbstractLine<Hologram> line) {
		for (int i = 0; i < lines.size(); i++) {
			Line hline = lines.get(i);
			if (hline.line == line) {
				hline.destroy();
				for (int j = 0; j < i; j++) {
					hline = lines.get(j);
					hline.entity.teleport(hline.entity.getLocation().subtract(0, LINE_SPACING, 0));
				}
				lines.remove(i);
				break;
			}
		}
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
			this.entity = bottom.getWorld().spawn(bottom, ArmorStand.class);
			entity.setGravity(false);
			entity.setMarker(true);
			entity.setSmall(true);
			entity.setVisible(false);
			entity.setInvulnerable(true);
			line.addHolder(Hologram.this);
			updateText();
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
