package fr.olympa.api.common.observable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import fr.olympa.api.LinkSpigotBungee;

@SuppressWarnings("rawtypes")
public class ObservableMap<K, V> extends AbstractObservable implements Map<K, V> {
	public static final ObservableMap EMPTY_MAP = new ObservableMap<>(Collections.emptyMap());

	public void putAllFromJson(String json) {
		if (json != null && !json.isBlank() && !json.equals("{}"))
			putAll(LinkSpigotBungee.getInstance().getGson().fromJson(json, new TypeToken<Map<K, V>>() {}.getType()));
		putAll(new Gson().fromJson(json, new TypeToken<Map<K, V>>() {}.getType()));
	}

	private final Map<K, V> sub;

	public ObservableMap(Map<K, V> map) {
		this.sub = map;
	}

	public Map<K, V> getSubMap() {
		return sub;
	}

	public String toJson() {
		return LinkSpigotBungee.getInstance().getGson().toJson(sub);
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
	public boolean containsKey(Object key) {
		return sub.containsKey(key);
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return sub.entrySet();
	}

	@Override
	public Set<K> keySet() {
		return sub.keySet();
	}

	@Override
	public Collection<V> values() {
		return sub.values();
	}

	public Stream<Entry<K, V>> stream() {
		return entrySet().stream();
	}

	@Override
	public void clear() {
		sub.clear();
		update();
	}

	@Override
	public V remove(Object key) {
		V removed = sub.remove(key);
		update();
		if (key instanceof AbstractObservable observable)
			observable.clearObservers();
		if (removed instanceof AbstractObservable observable)
			observable.clearObservers();
		return removed;
	}

	public List<V> removeAll(Iterable<K> keys) {
		List<V> removeds = new ArrayList<>();
		keys.forEach(key -> {
			V removed = sub.remove(key);
			if (removed != null)
				removeds.add(removed);
			else
				return;
			if (key instanceof AbstractObservable observable)
				observable.clearObservers();
			if (removed instanceof AbstractObservable observable)
				observable.clearObservers();
		});
		update();
		return removeds;
	}

	@Override
	public boolean containsValue(Object value) {
		return sub.containsValue(value);
	}

	@Override
	public V put(K key, V value) {
		V returnValue = sub.put(key, value);
		if (key instanceof AbstractObservable observable)
			observable.observe("parent_map", () -> update());
		if (value instanceof AbstractObservable observable)
			observable.observe("parent_map", () -> update());
		update();
		return returnValue;
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		sub.putAll(m);
		m.forEach((key, value) -> {
			if (key instanceof AbstractObservable observable)
				observable.observe("parent_map", () -> update());
			if (value instanceof AbstractObservable observable)
				observable.observe("parent_map", () -> update());
		});
		update();
	}

	@Override
	public V get(Object key) {
		return sub.get(key);
	}
}
