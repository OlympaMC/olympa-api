package fr.olympa.api.lines;

public interface LinesHolder<T extends LinesHolder<T>> {

	void update(AbstractLine<T> line, String value);

	String getName();

	void addLine(AbstractLine<T> line);

	void initScoreboard();

	void unload();

	void run();

}
