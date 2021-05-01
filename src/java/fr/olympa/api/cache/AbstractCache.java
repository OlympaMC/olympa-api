package fr.olympa.api.cache;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;

import fr.olympa.api.task.NativeTask;

abstract class AbstractCache<K, T> {

	protected static final NativeTask task = NativeTask.getInstance();

	@Nonnull
	protected Cache<K, T> objectsCached;
	@Nonnull
	private Function<K, T> asyncGetObjectFunction;
	@Nonnull
	int timeBeforeRemove;
	@Nonnull
	TimeUnit unit;

	protected AbstractCache(UnaryOperator<CacheBuilder<Object, Object>> builder, Function<K, T> asyncGetObjectFunction, int timeBeforeRemove, TimeUnit unit) {
		this.objectsCached = builder.apply(CacheBuilder.newBuilder()).build();
		this.asyncGetObjectFunction = asyncGetObjectFunction;
		this.timeBeforeRemove = timeBeforeRemove;
		this.unit = unit;
	}

	protected AbstractCache(Function<K, T> asyncGetObjectFunction, int timeBeforeRemove, TimeUnit unit) {
		this(cb -> cb.recordStats().expireAfterAccess(timeBeforeRemove, unit), asyncGetObjectFunction, timeBeforeRemove, unit);
	}

	private void privatePut(K key, T object) {
		objectsCached.put(key, object);
	}

	@Nullable
	public T getObjectCached(K key) {
		return objectsCached.getIfPresent(key);
	}

	/**
	 * Need to be async
	 */
	@Nullable
	public T getObjectNotCached(K key) {
		T t = asyncGetObjectFunction.apply(key);
		if (t != null)
			privatePut(key, t);
		return t;
	}

	/**
	 * Need to be async
	 */
	@Nullable
	public T getObjectWithoutCached(K key) {
		T t = getObjectCached(key);
		if (t == null)
			t = getObjectNotCached(key);
		return t;
	}

	/**
	 *
	 * @param key
	 * @param callback async if object is not in cache. Object T in callback can be null
	 * @return true if key is in cache
	 */
	public boolean get(K key, Consumer<T> callback) {
		T t1 = getObjectCached(key);
		if (t1 != null) {
			callback.accept(t1);
			return true;
		} else {
			task.runTaskAsynchronously(() -> callback.accept(getObjectNotCached(key)));
			return false;
		}
	}

	public ConcurrentMap<K, T> getObjectsCached() {
		return objectsCached.asMap();
	}

	public CacheStats getStats() {
		return objectsCached.stats();
	}

}
