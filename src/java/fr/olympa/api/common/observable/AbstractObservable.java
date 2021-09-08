package fr.olympa.api.common.observable;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import fr.olympa.core.spigot.OlympaCore;

public abstract class AbstractObservable implements Observable {

	private final Map<String, Observer> observers = new HashMap<>(3);

	@Override
	public void observe(String name, Observer observer) {
		observers.put(name, observer);
	}

	@Override
	public void unobserve(String name) {
		observers.remove(name);
	}

	protected void clearObservers() {
		observers.clear();
	}

	protected boolean update() {
		boolean success = true;
		for (Entry<String, Observer> entry : observers.entrySet()) {
			try {
				entry.getValue().changed();
			}catch (Exception e) {
				System.err.printf("Une erreur est survenue lors de la mise à jour de l'observateur %s.%n", entry.getKey());
				e.printStackTrace();
				success = false;
			}
		}
		return success;
	}

}