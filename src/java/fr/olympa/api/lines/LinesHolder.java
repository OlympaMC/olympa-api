package fr.olympa.api.lines;

public interface LinesHolder<T extends LinesHolder<T>> {

	public void update(AbstractLine<T> line, String value);

	public String getName();
	
}
