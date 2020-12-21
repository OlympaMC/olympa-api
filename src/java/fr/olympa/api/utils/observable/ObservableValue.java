package fr.olympa.api.utils.observable;

import java.util.function.Function;
import java.util.function.Supplier;

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
	
	public boolean isEmpty() {
		return value == null;
	}
	
	public T or(T defaultValue) {
		return value == null ? defaultValue : value;
	}
	
	public T or(Supplier<T> defaultValueSupplier) {
		return value == null ? defaultValueSupplier.get() : value;
	}
	
	public <K> K map(Function<T, K> mapper) {
		return mapper.apply(value);
	}
	
	public <K> K mapOr(Function<T, K> mapper, K defaultMapping) {
		return value == null ? defaultMapping : mapper.apply(value);
	}
	
}
