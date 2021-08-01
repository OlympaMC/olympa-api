package fr.olympa.api.common.observable;

public class ObservableDouble extends AbstractObservable implements Comparable<ObservableDouble> {
	
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
	
	public float getAsFloat() {
		return (float) value;
	}
	
	@Override
	public int compareTo(ObservableDouble o) {
		return compareTo(o.value);
	}
	
	public int compareTo(double o) {
		return Double.compare(value, o);
	}
	
}
