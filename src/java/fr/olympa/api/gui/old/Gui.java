package fr.olympa.api.gui.old;

@Deprecated
public class Gui {

	private final String name;
	private final int lines;
	private final int columns;
	private final int size;

	protected Gui(final String name, final int lines, final int columns) {
		this.name = name;
		this.lines = lines;
		this.columns = columns;
		this.size = columns * lines;
	}

	/**
	 * @return the columns
	 */
	public int getColumns() {
		return this.columns;
	}

	/**
	 * @return the lines
	 */
	public int getLines() {
		return this.lines;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return the size
	 */
	public int getSize() {
		return this.size;
	}
}
