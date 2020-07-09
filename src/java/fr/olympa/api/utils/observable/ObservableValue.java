package fr.olympa.api.utils.observable;

public class ObservableValue<T> extends AbstractObservable {
	
	private T value;
	
	public ObservableValue(T value) {
		this.value = value;
	}
	
	public void set(T value) {
		this.value = value;
		update();
	}
	
	public T get() {
		return value;
	}
	
}
