package fr.olympa.api.common.observable;

public class ObservableInt extends AbstractObservable implements Comparable<ObservableInt> {
	
	private int value;
	
	public ObservableInt(int value) {
		this.value = value;
	}
	
	public void set(int value) {
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
	
	public void add(int value) {
		this.value += value;
		update();
	}
	
	public void substract(int value) {
		this.value -= value;
		update();
	}
	
	public int get() {
		return value;
	}
	
	public double getAsDouble() {
		return (double) value;
	}
	
	@Override
	public int compareTo(ObservableInt o) {
		return compareTo(o.value);
	}
	
	public int compareTo(int o) {
		return Integer.compare(value, o);
	}
	
}
