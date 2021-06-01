package fr.olympa.api.spigot.lines;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import fr.olympa.core.spigot.OlympaCore;

public abstract class AbstractLine<T extends LinesHolder<T>> {

	private Map<T, String> holders = Collections.synchronizedMap(new HashMap<>());

	public abstract String getValue(T holder);
	
	public void addHolder(T holder) {
		holders.put(holder, null);
	}

	public void removeHolder(T holder) {
		holders.remove(holder);
	}
	
	public Set<T> getHolders() {
		return holders.keySet();
	}

	public void updateGlobal() {
		for (Entry<T, String> holderEntry : holders.entrySet()) {
			updateHolder(holderEntry);
		}
	}

	public void updateHolder(T holder) {
		for (Entry<T, String> holderEntry : holders.entrySet()) {
			if (holderEntry.getKey() == holder) {
				updateHolder(holderEntry);
				return;
			}
		}
	}
	
	private void updateHolder(Entry<T, String> holderEntry) {
		try {
			String newValue = getValue(holderEntry.getKey());
			if (!newValue.equals(holderEntry.getValue())) {
				holderEntry.getKey().update(this, newValue);
				holderEntry.setValue(newValue);
			}
		}catch (Exception ex) {
			OlympaCore.getInstance().sendMessage("Une erreur est survenue lors de la mise à jour de l'holder %s (dernière valeur: %s)", holderEntry.getKey().getName(), holderEntry.getValue());
			ex.printStackTrace();
		}
	}

}
