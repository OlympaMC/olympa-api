package fr.olympa.api.spigot.lines;

import java.util.function.Supplier;

public class FixedUpdatableLine<T extends LinesHolder<T>> extends AbstractLine<T> {
	
	private Supplier<String> valueSupplier;
	private String value;
	
	public FixedUpdatableLine(Supplier<String> valueSupplier) {
		this.valueSupplier = valueSupplier;
	}
	
	@Override
	public void updateGlobal() {
		value = valueSupplier.get();
		super.updateGlobal();
	}
	
	@Override
	public String getValue(T holder) {
		if (value == null) value = valueSupplier.get();
		return value;
	}
	
}
