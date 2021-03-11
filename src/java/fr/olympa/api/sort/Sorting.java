package fr.olympa.api.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class Sorting<T> implements Comparator<T> {

	Map<ToLongFunction<T>, Boolean> sortArgs;

	public Sorting(ToLongFunction<T> sortArg) {
		this(Map.of(sortArg, true));
	}

	public Sorting(ToLongFunction<T> sortArg, boolean ascending) {
		this(Map.of(sortArg, ascending));
	}

	public Sorting(ToLongFunction<T>... sortingArgs) {
		this(Arrays.stream(sortingArgs).collect(Collectors.toMap(f -> f, f -> true, (x, y) -> y, LinkedHashMap::new)));
	}

	public Sorting(boolean ascending, ToLongFunction<T>... sortingArgs) {
		this(Arrays.stream(sortingArgs).collect(Collectors.toMap(f -> f, f -> ascending, (x, y) -> y, LinkedHashMap::new)));
	}

	public Sorting(Map<ToLongFunction<T>, Boolean> sortArgs) {
		this.sortArgs = sortArgs;
	}

	@Override
	public int compare(T o1, T o2) {
		int compared = 0;
		for (Entry<ToLongFunction<T>, Boolean> entry : sortArgs.entrySet()) {
			ToLongFunction<T> f = entry.getKey();
			entry.getKey().applyAsLong(o1);
			if (entry.getValue())
				compared = Long.compare(entry.getKey().applyAsLong(o1), f.applyAsLong(o2));
			else
				compared = Long.compare(entry.getKey().applyAsLong(o2), f.applyAsLong(o1));
			if (compared != 0)
				break;
		}
		return compared;
	}

}
