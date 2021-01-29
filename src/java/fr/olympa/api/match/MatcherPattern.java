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

public class MatcherPattern {

	public static final Cache<String, Pattern> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	static {
		CacheStats.addCache("REGEX_PATTERN", cache);
	}
	final Cache<String, Boolean> cacheIs;
	final String regex;
	Function<String, Object> supplyArgumentFunction;
	String typeName = "Unknown Type";

	private static Pattern getPattern(String regex) {
		Pattern pattern = cache.getIfPresent(regex);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			cache.put(regex, pattern);
		}
		return pattern;
	}

	public Pattern getPattern() {
		return getPattern(regex);
	}

	public MatcherPattern(String regex, Function<String, Object> supplyArgumentFunction, Class<?> typeClass) {
		this(regex);
		this.supplyArgumentFunction = supplyArgumentFunction;
		typeName = typeClass.getName();
	}

	public MatcherPattern(String regex, Function<String, Object> supplyArgumentFunction) {
		this(regex);
		this.supplyArgumentFunction = supplyArgumentFunction;
	}

	public MatcherPattern(String regex) {
		this.regex = regex;
		cacheIs = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();
		CacheStats.addCache("REGEX_PATTERN_" + regex, cacheIs);
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

	public List<Object> extractsAndParse(String text) throws IllegalArgumentException {
		Matcher matcher = getPattern().matcher(text);
		List<Object> list = new ArrayList<>();
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
	public Object extractAndParse(String text) {
		return parse(extract(text));
	}

	/**
	* Parse object
	*/
	public Object parse(String string) throws IllegalArgumentException {
		//		throw new NullPointerException("supplyArgumentFunction cannot be null while using MatcherPattern#parse(string).");
		if (is(string))
			if (supplyArgumentFunction != null)
				return supplyArgumentFunction.apply(string);
			else
				return string;
		// TODO ajouter le type demandé sans variable
		throw new IllegalArgumentException(String.format("%s is not matching the regex of %s, cannot parse to type.", string, typeName));
	}

	/**
	 * Contains in text
	 */
	public boolean contains(String text) {
		return getPattern(wholeWord(false)).matcher(text).find();
	}

	/**
	 * Match all text
	 */
	public boolean is(String text) {
		Boolean is = cacheIs.getIfPresent(text);
		if (is == null) {
			is = getPattern(wholeWord(true)).matcher(text).find();
			cacheIs.put(text, is);
		}
		return is;
	}

}
