package fr.olympa.api.common.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

public class Sorting<T> implements Comparator<T> {

	Map<ToLongFunction<T>, Boolean> sortArgs;

	public Sorting(ToLongFunction<T> sortArg) {
		this(Map.of(sortArg, true));
	}

	public Sorting(ToLongFunction<T> sortArg, boolean downward) {
		this(Map.of(sortArg, downward));
	}

	@SafeVarargs
	public Sorting(ToLongFunction<T>... sortingArgs) {
		this(Arrays.stream(sortingArgs).collect(Collectors.toMap(f -> f, f -> true, (x, y) -> y)));
	}

	@SafeVarargs
	public Sorting(boolean downward, ToLongFunction<T>... sortingArgs) {
		this(Arrays.stream(sortingArgs).collect(Collectors.toMap(f -> f, f -> downward, (x, y) -> y)));
	}

	public Sorting(Map<ToLongFunction<T>, Boolean> sortArgs) {
		this.sortArgs = sortArgs;
	}

	@Override
	public int compare(T o1, T o2) {
		int compared = 0;
		for (Entry<ToLongFunction<T>, Boolean> entry : sortArgs.entrySet()) {
			ToLongFunction<T> f = entry.getKey();
			if (entry.getValue())
				compared = Long.compare(f.applyAsLong(o1), f.applyAsLong(o2));
			else
				compared = Long.compare(f.applyAsLong(o2), f.applyAsLong(o1));
			if (compared != 0)
				break;
		}
		return compared;
	}

}
