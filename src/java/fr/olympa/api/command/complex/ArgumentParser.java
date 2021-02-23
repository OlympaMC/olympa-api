package fr.olympa.api.command.complex;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collection;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ArgumentParser<T> {

	private BiFunction<T, String, Collection<String>> tabArgumentsFunction;
	public Function<String, Object> supplyArgumentFunction;
	public Function<String, String> wrongArgTypeMessageFunction;
	Cache<Entry<T, String>, Collection<String>> cache;

	/**
	 *
	 * @Deprecated
	 *
	 * @param tabArgumentsFunction
	 * @param supplyArgumentFunction
	 * @param wrongArgTypeMessageFunction Le message ne doit pas finir par un point, et doit avoir un sens en utiliser le message suivie d'un ou (ex: Ton message d'erreur OU un autre message d'erreur)
	 */
	@Deprecated(forRemoval = true)
	public ArgumentParser(Function<T, Collection<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, UnaryOperator<String> wrongArgTypeMessageFunction, boolean hasCache) {
		this(supplyArgumentFunction, wrongArgTypeMessageFunction, hasCache);
		this.tabArgumentsFunction = (t, u) -> tabArgumentsFunction.apply(t);
	}

	/**
	 *
	 * @param tabArgumentsFunction
	 * @param supplyArgumentFunction
	 * @param wrongArgTypeMessageFunction Le message ne doit pas finir par un point, et doit avoir un sens en utiliser le message suivie d'un ou (ex: Ton message d'erreur OU un autre message d'erreur)
	 */
	public ArgumentParser(BiFunction<T, String, Collection<String>> tabArgumentsFunction, Function<String, Object> supplyArgumentFunction, UnaryOperator<String> wrongArgTypeMessageFunction, boolean hasCache) {
		this(supplyArgumentFunction, wrongArgTypeMessageFunction, hasCache);
		this.tabArgumentsFunction = tabArgumentsFunction;
	}

	/**
	 *
	 * @param tabArgumentsFunction
	 * @param supplyArgumentFunction
	 * @param wrongArgTypeMessageFunction Le message ne doit pas finir par un point, et doit avoir un sens en utiliser le message suivie d'un ou (ex: Ton message d'erreur OU un autre message d'erreur)
	 */
	private ArgumentParser(Function<String, Object> supplyArgumentFunction, UnaryOperator<String> wrongArgTypeMessageFunction, boolean hasCache) {
		this.supplyArgumentFunction = supplyArgumentFunction;
		this.wrongArgTypeMessageFunction = wrongArgTypeMessageFunction;
		if (hasCache)
			cache = CacheBuilder.newBuilder().expireAfterWrite(60, TimeUnit.SECONDS).build();
		//		this.hasCache = hasCache;
	}

	public Collection<String> applyTab(T t, String arg) {
		if (cache == null)
			return applyTabWithoutCache(t, arg);
		Collection<String> r = cache.getIfPresent(t);
		SimpleEntry<T, String> entry = new AbstractMap.SimpleEntry<>(t, arg);
		if (r == null) {
			r = cache.asMap().entrySet().stream().filter(e -> e.getKey().getKey().equals(t) && e.getKey().getValue().startsWith(arg)).findFirst()
					.map(e -> e.getValue().stream().filter(s -> arg.startsWith(s)).collect(Collectors.toList())).orElse(null);
			if (r == null) {
				r = applyTabWithoutCache(t, arg);
				if (r != null && !r.isEmpty())
					cache.put(entry, r);
			} else if (!r.isEmpty())
				cache.put(entry, r);
		}
		return r;
	}

	public Collection<String> applyTabWithoutCache(T t, String arg) {
		return tabArgumentsFunction.apply(t, arg);
	}
}
