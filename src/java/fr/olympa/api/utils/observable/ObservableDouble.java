package fr.olympa.api.utils.observable;

public class ObservableDouble extends AbstractObservable {
	
	private double value;
	
	public ObservableDouble(double value) {
		this.value = value;
	}
	
	public void set(double value) {
		this.value = value;
		update();
	}
	
	public void increment() {
		value++;
		update();
	}
	
	public void decrement() {
		value--;
		update();
	}
	
	public void add(double value) {
		this.value += value;
		update();
	}
	
	public void substract(double value) {
		this.value -= value;
		update();
	}
	
	public double get() {
		return value;
	}
	
}
