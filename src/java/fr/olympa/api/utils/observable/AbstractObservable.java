package fr.olympa.api.utils.observable;

import java.util.HashMap;
import java.util.Map;

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

	protected void update() {
		observers.forEach((x, y) -> y.changed());
	}

}