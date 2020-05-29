package fr.olympa.api.utils.observable;

public interface Observable {

	void observe(String name, Observer observer);

	void unobserve(String name);

	@FunctionalInterface
	public interface Observer {
		void changed();
	}

}