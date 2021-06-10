package fr.olympa.api.common.observable;

public class ObservableBoolean extends AbstractObservable implements Comparable<ObservableBoolean> {
	
	private boolean value;
	
	public ObservableBoolean(boolean value) {
		this.value = value;
	}
	
	public void set(boolean value) {
		this.value = value;
		update();
	}
	
	public void toggle() {
		value = !value;
		update();
	}
	
	public boolean get() {
		return value;
	}
	
	public void execute(Runnable ifTrue, Runnable ifFalse) {
		if (value) {
			if (ifTrue != null) ifTrue.run();
		}else if (ifFalse != null) ifFalse.run();
	}
	
	@Override
	public int compareTo(ObservableBoolean o) {
		return compareTo(o.value);
	}
	
	public int compareTo(boolean o) {
		return Boolean.compare(value, o);
	}
	
}
