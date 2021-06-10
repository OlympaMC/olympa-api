package fr.olympa.api.utils;

import com.google.common.cache.Cache;

public class CacheUtils<K, V> {

	Cache<K, V> cache;

	public CacheUtils(Cache<K, V> cache) {
		this.cache = cache;
	}

	public V getIfCached(K key, V value) {
		V oldValue = cache.getIfPresent(key);
		if (oldValue != null)
			return oldValue;
		cache.put(key, value);
		return null;
	}

	public V getCache(K key) {
		V oldValue = cache.getIfPresent(key);
		if (oldValue != null)
			return oldValue;
		return null;
	}
}
