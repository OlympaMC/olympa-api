package fr.olympa.api.spigot.lines;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import fr.olympa.core.spigot.OlympaCore;

public abstract class AbstractLine<T extends LinesHolder<T>> {

	private Map<T, String> holders = Collections.synchronizedMap(new HashMap<>());

	private ReadWriteLock lock = new ReentrantReadWriteLock();
	
	public abstract String getValue(T holder);
	
	public void addHolder(T holder) {
		lock.writeLock().lock();
		holders.put(holder, null);
		lock.writeLock().unlock();
	}

	public void removeHolder(T holder) {
		lock.writeLock().lock();
		holders.remove(holder);
		lock.writeLock().unlock();
	}
	
	public boolean hasHolders() {
		return holders.isEmpty();
	}

	public void updateGlobal() {
		lock.readLock().lock();
		for (Entry<T, String> holderEntry : holders.entrySet()) {
			updateHolder(holderEntry);
		}
		lock.readLock().unlock();
	}

	public void updateHolder(T holder) {
		lock.readLock().lock();
		for (Entry<T, String> holderEntry : holders.entrySet()) {
			if (holderEntry.getKey() == holder) {
				updateHolder(holderEntry);
				break;
			}
		}
		lock.readLock().unlock();
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
