package fr.olympa.api.common.cache;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.annotation.Nonnull;

/**
 * Le but ici est d'extends cette class et ajouter les method pour récupérer l'object à partir d'un field typé uniquement
 */
public abstract class CacheMultiKeys<T, K> extends AbstractCache<K, T> {

	@Nonnull
	private Function<T, K> asyncGetKeyFunction;

	/**
	 * Create a map with cached objects and a way to retrieve the object via any field of that object when they are have they method created in the class extends by CacheMultiKeys.
	 * or a way to recover them through @asyncGetObjectFunction if they not in cache
	 *
	 * You need to extends this class to add method to getFromCache or getFromAsync from all field you want.
	 */
	public CacheMultiKeys(Function<K, T> asyncGetObjectFunction, Function<T, K> asyncGetKeyFunction, int timeBeforeRemove, TimeUnit unit) {
		super(asyncGetObjectFunction, timeBeforeRemove, unit);
		this.asyncGetKeyFunction = asyncGetKeyFunction;
	}

	public void getObjectByOtherFieldWithoutFunctionsCached(Object otherField, Consumer<T> callback, Function<T, Object> getFieldWithObject, Function<Object, T> asyncGetObjectWithField) {
		T tCached = objectsCached.asMap().values().stream().filter(t -> {
			Object field = getFieldWithObject.apply(t);
			return field != null && field.equals(otherField);
		}).findFirst().orElse(null);
		if (tCached != null)
			callback.accept(tCached);
		else
			task.runTaskAsynchronously(() -> {
				T tNotCached = asyncGetObjectWithField.apply(otherField);
				if (tNotCached != null)
					add(tCached);
				callback.accept(tNotCached);
			});
	}

	public boolean add(T object) {
		if (asyncGetKeyFunction == null)
			return false;
		objectsCached.put(asyncGetKeyFunction.apply(object), object);
		return true;
	}
}
