package fr.olympa.api.utils;

import java.util.function.Consumer;

public class ConsumerCount<T> {

	Consumer<T> c;
	int i = 0;
	int iMax;

	/**
	 * @param c
	 * @param iMax start to 0
	 */
	public ConsumerCount(Consumer<T> c, int iMax) {
		this.c = c;
		this.iMax = iMax;
	}

	public Consumer<T> get() {
		return c;
	}

	public void accept(T t) {
		if (i >= iMax) {
			c.accept(t);
			i = 0;
		}
	}
}
