package fr.olympa.api.common.observable;

public class ObservableBoolean extends AbstractObservable {
	
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
	
}
