package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.List;

public abstract class Observable {

	private final List<Observer> observers = new ArrayList<>(3);

	public void observe(Observer observer) {
		observers.add(observer);
	}

	public void remove(Observer observer) {
		observers.remove(observer);
	}

	protected void update() {
		observers.forEach(Observer::changed);
	}

	public interface Observer {
		void changed();
	}

}
