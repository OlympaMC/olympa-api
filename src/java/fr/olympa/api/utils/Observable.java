package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.List;

public interface Observable {

	void observe(Observer observer);

	void remove(Observer observer);

	public abstract class AbstractObservable implements Observable {

		private final List<Observer> observers = new ArrayList<>(3);

		@Override
		public void observe(Observer observer) {
			observers.add(observer);
		}

		@Override
		public void remove(Observer observer) {
			observers.remove(observer);
		}

		protected void update() {
			observers.forEach(Observer::changed);
		}

	}

	@FunctionalInterface
	public interface Observer {
		void changed();
	}

}