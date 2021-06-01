package fr.olympa.api.common.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import fr.olympa.api.common.task.NativeTask;

/**
 * Use to hide 'put' in other type of Cache
 */
public class BasicCache<K, T> extends AbstractCache<K, T> {

	protected static final NativeTask task = NativeTask.getInstance();

	public BasicCache(Function<K, T> asyncGetObjectFunction, int timeBeforeRemove, TimeUnit unit) {
		super(asyncGetObjectFunction, timeBeforeRemove, unit);
	}

	public void put(K key, T object) {
		objectsCached.put(key, object);
	}

}
