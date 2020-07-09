package fr.olympa.api.lines;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractLine<T extends LinesHolder<T>> {

	private Map<T, String> holders = new HashMap<>();

	public abstract String getValue(T holder);
	
	public void addHolder(T holder) {
		holders.put(holder, null);
	}

	public void removeHolder(T holder) {
		holders.remove(holder);
	}

	public void updateGlobal() {
		for (Entry<T, String> scoreboard : holders.entrySet()) {
			T holder = scoreboard.getKey();
			String newValue = getValue(holder);
			if (!newValue.equals(scoreboard.getValue())) {
				holder.update(this, newValue);
				scoreboard.setValue(newValue);
			}
		}
	}

	public void updateHolder(T holder) {
		for (Entry<T, String> scoreboard : holders.entrySet()) {
			T sholder = scoreboard.getKey();
			if (sholder == holder) {
				String newValue = getValue(holder);
				if (!newValue.equals(scoreboard.getValue())) {
					holder.update(this, newValue);
					scoreboard.setValue(newValue);
				}
				return;
			}
		}
	}

}
