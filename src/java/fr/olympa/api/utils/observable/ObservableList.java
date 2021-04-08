package fr.olympa.api.utils.observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

@SuppressWarnings("rawtypes")
public class ObservableList<T> extends AbstractObservable implements List<T> {

	public static final ObservableList EMPTY_LIST = new ObservableList<>(Collections.emptyList());
	public static final CollectorObservable COLLECTOR = new CollectorObservable<>();

	@SuppressWarnings("unchecked")
	public static <A, B> CollectorObservable<A, B> getCollector() {
		return COLLECTOR;
	}

	private final List<T> sub;

	public ObservableList(List<T> list) {
		this.sub = list;
	}

	public List<T> getSubList() {
		return sub;
	}

	@Override
	public int size() {
		return sub.size();
	}

	@Override
	public boolean isEmpty() {
		return sub.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return sub.contains(sub);
	}

	@Override
	public Iterator<T> iterator() {
		return sub.iterator();
	}

	@Override
	public Object[] toArray() {
		return sub.toArray();
	}

	@SuppressWarnings("hiding")
	@Override
	public <T> T[] toArray(T[] a) {
		return sub.toArray(a);
	}

	@Override
	public boolean add(T e) {
		boolean add = sub.add(e);
		update();
		return add;
	}

	@Override
	public boolean remove(Object o) {
		boolean remove = sub.remove(o);
		if (remove)
			update();
		return remove;
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return sub.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		boolean addAll = sub.addAll(c);
		update();
		return addAll;
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		boolean addAll = sub.addAll(index, c);
		update();
		return addAll;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean removeAll = sub.removeAll(c);
		update();
		return removeAll;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean retainAll = sub.retainAll(c);
		update();
		return retainAll;
	}

	@Override
	public void clear() {
		sub.clear();
		update();
	}

	@Override
	public T get(int index) {
		return sub.get(index);
	}

	@Override
	public T set(int index, T element) {
		T set = sub.set(index, element);
		update();
		return set;
	}

	@Override
	public void add(int index, T element) {
		sub.add(index, element);
		update();
	}

	@Override
	public T remove(int index) {
		T remove = sub.remove(index);
		update();
		return remove;
	}

	@Override
	public int indexOf(Object o) {
		return sub.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return sub.lastIndexOf(o);
	}

	@Override
	public ListIterator<T> listIterator() {
		return sub.listIterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return sub.listIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return sub.subList(fromIndex, toIndex);
	}

	public static class CollectorObservable<T, R> implements Collector<T, ObservableList<T>, R> {

		private Supplier<ObservableList<T>> supplier = () -> new ObservableList<>(new ArrayList<>());
		private BiConsumer<ObservableList<T>, T> accumulator = ObservableList::add;
		private BinaryOperator<ObservableList<T>> combiner = (left, right) -> {
			left.addAll(right);
			return left;
		};
		private Function<ObservableList<T>, R> finisher = (x) -> (R) x;
		private EnumSet<Characteristics> characteristics = EnumSet.of(Characteristics.IDENTITY_FINISH);

		@Override
		public Supplier<ObservableList<T>> supplier() {
			return supplier;
		}

		@Override
		public BiConsumer<ObservableList<T>, T> accumulator() {
			return accumulator;
		}

		@Override
		public BinaryOperator<ObservableList<T>> combiner() {
			return combiner;
		}

		@Override
		public Function<ObservableList<T>, R> finisher() {
			return finisher;
		}

		@Override
		public Set<Characteristics> characteristics() {
			return characteristics;
		}

	}

}
