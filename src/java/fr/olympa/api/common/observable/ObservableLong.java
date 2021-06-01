package fr.olympa.api.common.observable;

public class ObservableLong extends AbstractObservable {
	
	private long value;
	
	public ObservableLong(long value) {
		this.value = value;
	}
	
	public void set(long value) {
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
	
	public void add(long value) {
		this.value += value;
		update();
	}
	
	public void substract(long value) {
		this.value -= value;
		update();
	}
	
	public long get() {
		return value;
	}
	
}
