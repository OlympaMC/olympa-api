package fr.olympa.api.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class DivideList<T> {

	List<T> originalList;
	Function<T, Boolean> filter;
	List<T> list1 = new ArrayList<>();
	List<T> list2 = new ArrayList<>();

	/**
	 * @param originalList
	 * @param filter
	 */
	public DivideList(List<T> originalList, Function<T, Boolean> filter) {
		this.originalList = originalList;
		this.filter = filter;
	}

	public DivideList<T> divide() {
		for (T t : originalList)
			if (filter.apply(t))
				list1.add(t);
			else
				list2.add(t);
		return this;
	}

	public List<T> getTrue() {
		return list1;
	}

	public List<T> getFalse() {
		return list2;
	}
}
