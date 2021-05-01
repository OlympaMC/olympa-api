package fr.olympa.api.match;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import fr.olympa.api.utils.CacheStats;

public class MatcherPattern<T> {

	public static final Cache<String, MatcherPattern<?>> cache = CacheBuilder.newBuilder().recordStats()/*.maximumWeight(100)*/.maximumSize(200).build();
	static {
		CacheStats.addCache("REGEX_PATTERN", cache);
	}
	private final Cache<String, Pattern> cachePattern = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
	private final String regex;
	private Function<String, T> supplyArgumentFunction;
	private String typeName = "Unknown Type";

	public static MatcherPattern<?> of(String regex) {
		MatcherPattern<?> matcherPattern = cache.getIfPresent(regex);
		if (matcherPattern == null) {
			matcherPattern = new MatcherPattern<>(regex);
			cache.put(regex, matcherPattern);
		}
		return matcherPattern;
	}

	private Pattern getPattern(String regex) {
		Pattern pattern = cachePattern.getIfPresent(regex);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			cachePattern.put(regex, pattern);
		}
		return pattern;
	}

	public Pattern getPattern() {
		return getPattern(regex);
	}

	public MatcherPattern(String regex, Function<String, T> supplyArgumentFunction, Class<?> typeClass) {
		this(regex, supplyArgumentFunction);
		typeName = typeClass.getName();
	}

	public MatcherPattern(String regex, Function<String, T> supplyArgumentFunction) {
		this(regex);
		this.supplyArgumentFunction = supplyArgumentFunction;
	}

	protected MatcherPattern(String regex) {
		this.regex = regex;
	}

	private String wholeWord(boolean wholeWord) {
		String tmp = regex;
		if (wholeWord) {
			if (tmp.indexOf(0) != '^')
				tmp = "^" + tmp;
			if (regex.indexOf(tmp.length() - 1) != '$')
				tmp = tmp + "$";
		} else {
			if (tmp.indexOf(0) == '^')
				tmp = tmp.substring(1);
			if (tmp.indexOf(tmp.length() - 1) == '$')
				tmp = tmp.substring(0, regex.length() - 2);
		}
		return tmp;
	}

	public List<T> extractsAndParse(String text) throws IllegalArgumentException {
		Matcher matcher = getPattern().matcher(text);
		List<T> list = new ArrayList<>();
		while (matcher.find())
			list.add(parse(matcher.group()));
		return list;
	}

	/**
	 * Extract alls occurance
	 */
	public List<String> extracts(String text) {
		Matcher matcher = getPattern().matcher(text);
		List<String> list = new ArrayList<>();
		while (matcher.find())
			list.add(matcher.group());
		return list;
	}

	/**
	 * replace
	 */
	public String replace(String text, String replaced) {
		Matcher matcher = getPattern().matcher(text);
		while (matcher.find())
			text = text.replace(matcher.group(), replaced);
		return text;
	}

	/**
	 * Extract first occurances
	 */
	public String extract(String text) {
		Matcher matcher = getPattern().matcher(text);
		if (matcher.find())
			return matcher.group();
		return null;
	}

	/**
	 * Extract first occurances
	 */
	public T extractAndParse(String text) {
		return parse(extract(text));
	}

	/**
	* Parse object
	*/
	@SuppressWarnings("unchecked")
	public T parse(String string) throws IllegalArgumentException {
		//		throw new NullPointerException("supplyArgumentFunction cannot be null while using MatcherPattern#parse(string).");
		if (is(string))
			if (supplyArgumentFunction != null)
				return supplyArgumentFunction.apply(string);
			else
				return (T) string;
		// TODO ajouter le type demandé sans variable
		throw new IllegalArgumentException(String.format("%s is not matching the regex of %s, cannot parse to type.", string, typeName));
	}

	/**
	 * Contains in text
	 */
	public boolean contains(String text) {
		return getPattern(wholeWord(false)).matcher(text).find();
	}

	public boolean contains(String text, int min) {
		Matcher matcher = getPattern(wholeWord(false)).matcher(text);
		while (min-- > 0)
			if (!matcher.find())
				return false;
		return true;
	}

	/**
	 * Match all text
	 */
	public boolean is(String text) {
		return getPattern(wholeWord(true)).matcher(text).find();
	}

}
