package fr.olympa.api.common.observable;

import java.util.Objects;

public class ObservableString extends AbstractObservable implements Comparable<ObservableString> {
	
	private String value;
	
	public ObservableString(String value) {
		this.value = value;
	}
	
	public void set(String value) {
		this.value = value;
		update();
	}
	
	public String get() {
		return value;
	}
	
	@Override
	public int compareTo(ObservableString o) {
		return compareTo(o.value);
	}
	
	public int compareTo(String o) {
		return Objects.compare(o, value, String::compareTo);
	}
	
}
