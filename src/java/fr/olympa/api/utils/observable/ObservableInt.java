package fr.olympa.api.utils.observable;

public class ObservableInt extends AbstractObservable {
	
	private int value;
	
	public ObservableInt(int value) {
		this.value = value;
	}
	
	public void set(int value) {
		this.value = value;
		update();
	}
	
	public void add(int value) {
		this.value += value;
		update();
	}
	
	public int get() {
		return value;
	}
	
}
