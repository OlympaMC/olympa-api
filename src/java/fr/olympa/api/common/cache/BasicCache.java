package fr.olympa.api.common.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import fr.olympa.api.common.task.NativeTask;

public class BasicCache<K, T> extends AbstractCache<K, T> {

	protected static final NativeTask task = NativeTask.getInstance();

	/**
	 * Create a map with cached objects and key and and a way to recover them through @asyncGetObjectFunction if they not in cache.
	 */
	public BasicCache(Function<K, T> asyncGetObjectFunction, int timeBeforeRemove, TimeUnit unit) {
		super(asyncGetObjectFunction, timeBeforeRemove, unit);
	}

	public void put(K key, T object) {
		objectsCached.put(key, object);
	}

}
