package fr.olympa.api.lines;

import java.util.function.Function;

import fr.olympa.api.utils.observable.Observable;

public class PlayerObservableLine<T extends LinesHolder<T>> extends AbstractLine<T> {

	private Function<T, String> value;
	private Function<T, Observable> observableProvider;

	public PlayerObservableLine(Function<T, String> value, Function<T, Observable> observableProvider) {
		this.value = value;
		this.observableProvider = observableProvider;
	}

	@Override
	public String getValue(T holder) {
		return value.apply(holder);
	}
	
	@Override
	public void addHolder(T holder) {
		super.addHolder(holder);
		observableProvider.apply(holder).observe("dynamic_line_" + hashCode(), () -> updateHolder(holder));
	}
	
	@Override
	public void removeHolder(T holder) {
		super.removeHolder(holder);
		observableProvider.apply(holder).unobserve("dynamic_line_" + hashCode());
	}

}
