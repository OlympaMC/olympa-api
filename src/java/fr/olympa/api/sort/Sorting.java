package fr.olympa.api.sort;

import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Sorting<T> implements Comparator<T> {

	Map<Function<T, Integer>, Boolean> sortArgs;

	public Sorting(Function<T, Integer>... sortArg) {
		this(Arrays.stream(sortArg).collect(Collectors.toMap(f -> f, f -> true, (x, y) -> y, LinkedHashMap::new)));
	}

	public Sorting(Map<Function<T, Integer>, Boolean> sortArgs) {
		this.sortArgs = sortArgs;
	}

	@Override
	public int compare(T o1, T o2) {
		int compared = 0;
		for (Entry<Function<T, Integer>, Boolean> entry : sortArgs.entrySet()) {
			Function<T, Integer> f = entry.getKey();
			if (entry.getValue())
				compared = entry.getKey().apply(o1).compareTo(f.apply(o2));
			else
				compared = entry.getKey().apply(o2).compareTo(f.apply(o1));
			if (compared != 0)
				break;
		}
		return compared;
	}

}
