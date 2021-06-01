package fr.olympa.api.common.observable;

public interface Observable {

	void observe(String name, Observer observer);

	void unobserve(String name);

	@FunctionalInterface
	public interface Observer {
		void changed() throws Exception;
	}

}