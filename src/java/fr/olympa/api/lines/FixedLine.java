package fr.olympa.api.lines;

public class FixedLine<T extends LinesHolder<T>> extends AbstractLine<T> {

	@SuppressWarnings ("rawtypes")
	public static final FixedLine EMPTY_LINE = new FixedLine<>("");

	private String value;

	public FixedLine(String value) {
		this.value = value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getValue(T holder) {
		return value;
	}

}
