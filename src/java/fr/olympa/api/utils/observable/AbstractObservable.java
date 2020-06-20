package fr.olympa.api.utils.observable;

import java.util.HashMap;
import java.util.Map;

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

	protected void update() {
		observers.forEach((x, y) -> {
			try {
				y.changed();
			}catch (Exception e) {
				OlympaCore.getInstance().getLogger().severe("Une erreur est survenue lors de la mise à jour de l'observateur " + x + ".");
				e.printStackTrace();
			}
		});
	}

}