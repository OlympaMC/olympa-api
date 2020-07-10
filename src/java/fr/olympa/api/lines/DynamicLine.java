package fr.olympa.api.lines;

import java.util.function.Function;

import fr.olympa.api.utils.observable.Observable;

public class DynamicLine<T extends LinesHolder<T>> extends AbstractLine<T> {

	private Function<T, String> value;

	public DynamicLine(Function<T, String> value) {
		this(value, null);
	}

	public DynamicLine(Function<T, String> value, Observable globalObservable) {
		this.value = value;

		if (globalObservable != null) globalObservable.observe("dynamic_line_" + hashCode(), this::updateGlobal);
	}

	@Override
	public String getValue(T holder) {
		return value.apply(holder);
	}

}
