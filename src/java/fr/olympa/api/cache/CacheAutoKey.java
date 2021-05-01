package fr.olympa.api.cache;

import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nullable;

/**
 * Le problème avec cette class, c'est que l'object peux pas être récupérer par deux field du même type
 */
public class CacheAutoKey<T, K> extends CacheMultiKeys<T, K> {

	private Map<Class<?>, Entry<Function<T, Object>, Function<Object, T>>> getObjectByOtherFieldFunctions = new HashMap<>();

	public CacheAutoKey(Function<K, T> asyncGetObjectFunction, Function<T, K> asyncGetKeyFunction, int timeBeforeRemove, TimeUnit unit) {
		super(asyncGetObjectFunction, asyncGetKeyFunction, timeBeforeRemove, unit);
	}

	/**
	 * @param otherFieldClass
	 * @param getFieldWithObject
	 * @param asyncGetObjectWithField
	 * @return false if getObjectByOtherFieldFunctions contains key otherFieldClass, true otherwise
	 */
	public boolean registerOtherFieldFunction(Class<?> otherFieldClass, Function<T, Object> getFieldWithObject, Function<Object, T> asyncGetObjectWithField) {
		if (getObjectByOtherFieldFunctions.containsKey(otherFieldClass))
			return false;
		getObjectByOtherFieldFunctions.put(otherFieldClass, new SimpleEntry<>(getFieldWithObject, asyncGetObjectWithField));
		return true;
	}

	/**
	 * @param otherField
	 * @param callback async if object is not in cache. Object T in callback can be null
	 * @return true if key is in cache, false if key was not, null if IllegalAccessException is trigger
	 */
	public Boolean getObjectByOtherField(Object otherField, Consumer<T> callback) {
		try {
			T t1;
			t1 = getObjectCachedByOtherField(otherField);
			if (t1 != null) {
				callback.accept(t1);
				return true;
			} else {
				task.runTaskAsynchronously(() -> {
					try {
						callback.accept(getObjectNotCachedByOtherField(otherField));
					} catch (IllegalAccessException e) {
						e.printStackTrace();
					}
				});
				return false;
			}
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Nullable
	public T getObjectCachedByOtherField(Object otherField) throws IllegalAccessException {
		Function<T, Object> f = getFunctionGetOtherFieldCached(otherField);
		if (f == null)
			throw new IllegalAccessException(String.format("Type of Object otherField %s was not set in getObjectFunctions", otherField.getClass().getSimpleName()));
		return objectsCached.asMap().values().stream().filter(t -> {
			Object field = f.apply(t);
			return field != null && field.equals(otherField);
		}).findFirst().orElse(null);
	}

	/**
	 * Need to be async
	 */
	@Nullable
	public T getObjectNotCachedByOtherField(Object otherField) throws IllegalAccessException {
		Function<Object, T> f = getFunctionGetOtherFieldNotCached(otherField);
		if (f == null)
			throw new IllegalAccessException(String.format("Type of Object otherField %s was not set in getObjectFunctions", otherField.getClass().getSimpleName()));
		return f.apply(otherField);
	}

	private Function<T, Object> getFunctionGetOtherFieldCached(Object object) {
		Entry<Function<T, Object>, Function<Object, T>> f = getObjectByOtherFieldFunctions.get(object.getClass());
		if (f == null)
			return null;
		return f.getKey();
	}

	private Function<Object, T> getFunctionGetOtherFieldNotCached(Object object) {
		Entry<Function<T, Object>, Function<Object, T>> f = getObjectByOtherFieldFunctions.get(object.getClass());
		if (f == null)
			return null;
		return f.getValue();
	}

}