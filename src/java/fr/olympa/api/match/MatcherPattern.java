package fr.olympa.api.match;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class MatcherPattern {

	public final static Cache<String, Pattern> cache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.HOURS).build();

	String regex;
	Function<String, Object> supplyArgumentFunction;
	String typeName = "Unknown Type";

	private Pattern getPattern() {
		return getPattern(regex);
	}

	public MatcherPattern(String regex, Function<String, Object> supplyArgumentFunction, Class<?> typeClass) {
		this.regex = regex;
		this.supplyArgumentFunction = supplyArgumentFunction;
		typeName = typeClass.getName();
	}

	public MatcherPattern(String regex, Function<String, Object> supplyArgumentFunction) {
		this.regex = regex;
		this.supplyArgumentFunction = supplyArgumentFunction;
	}

	public MatcherPattern(String regex) {
		this.regex = regex;
	}

	private Pattern getPattern(String regex) {
		Pattern pattern = cache.getIfPresent(regex);
		if (pattern == null) {
			pattern = Pattern.compile(regex);
			cache.put(regex, pattern);
		}
		return pattern;
	}

	private String wholeWord(boolean wholeWord) {
		String tmp = new String(regex);
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
		if (supplyArgumentFunction == null)
			throw new NullPointerException("supplyArgumentFunction cannot be null while using MatcherPattern#parse(string).");
		if (is(string))
			return supplyArgumentFunction.apply(string);
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
		return getPattern(wholeWord(true)).matcher(text).find();
	}

}
