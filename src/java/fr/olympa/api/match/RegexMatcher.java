package fr.olympa.api.match;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class RegexMatcher {
	public static final MatcherPattern USERNAME = new MatcherPattern("(?iu)^[\\w_]{3,16}$");
	public static final MatcherPattern IP = new MatcherPattern("((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}");

	public static final MatcherPattern FAKE_IP = new MatcherPattern("\\d{1,3}(\\.\\d{1,3}){3}");
	public static final MatcherPattern FAKE_UUID = new MatcherPattern("[0-9a-z]{8}-([0-9a-z]{4}-){3}[0-9a-z]{12}");
	public static final MatcherPattern EMAIL = new MatcherPattern("(.+)@(.+)\\.(.+)");
	public static final MatcherPattern DISCORD_TAG = new MatcherPattern(".{2,32}#[0-9]{4}");

	public static final MatcherPattern UUID = new MatcherPattern("[0-9a-f]{8}-?([0-9a-f]{4}-?){3}[0-9a-f]{12}", x -> {
		try {
			return java.util.UUID.fromString(x.contains("-") ? x : x.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
		} catch (IllegalArgumentException e) {
		}
		return null;
	}, java.util.UUID.class);

	public static final MatcherPattern DATE = new MatcherPattern("[0-9]{1,4}/[0-9]{1,2}/[0-9]{1,2}", x -> {
		try {
			return LocalDate.parse(x, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		} catch (DateTimeParseException e2) {
		}
		return null;
	});
	public static final MatcherPattern DOUBLE = new MatcherPattern("-?\\d+(.\\d+)?", x -> {
		try {
			return Double.parseDouble(x.replace(",", "."));
		} catch (NumberFormatException e) {
		}
		return null;
	});
	public static final MatcherPattern RELATIVE = new MatcherPattern("~-?\\d+(.\\d+)?", x -> {
		try {
			return Double.parseDouble(x.replace(",", "."));
		} catch (NumberFormatException e) {
		}
		return null;
	});
	public static final MatcherPattern FLOAT = new MatcherPattern("-?\\d+(.\\d+)?", x -> {
		try {
			return Float.parseFloat(x.replace(",", "."));
		} catch (NumberFormatException e) {
		}
		return null;
	});
	public static final MatcherPattern INT = new MatcherPattern("-?\\d+", x -> {
		try {
			return Integer.parseInt(x);
		} catch (NumberFormatException e) {
		}
		return null;
	}, Integer.class);
	public static final MatcherPattern LONG = new MatcherPattern("-?\\d+", x -> {
		try {
			return Long.parseLong(x);
		} catch (NumberFormatException e) {
		}
		return null;
	}, Long.class);
	//	public static MatcherPattern DATE = new MatcherPattern("[0-9]{1,4}/[0-9]{1,2}/[0-9]{1,2}", x -> LocalDate.parse(x, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	//	public static MatcherPattern DOUBLE = new MatcherPattern("-?\\d+(.\\d+)?", Double::parseDouble, Double.class);
	//	public static MatcherPattern INT = new MatcherPattern("-?\\d+", Integer::parseInt, Integer.class);
	//	public static MatcherPattern NUMBER = new MatcherPattern("\\d+", Integer::parseInt, Integer.class);
	//	public static MatcherPattern DIGIT = new MatcherPattern("\\d", x -> {
	//		Integer i = Integer.parseInt(x);
	//		if (i >= 0 && i <= 10)
	//			return i;
	//		return null;
	//	}, Integer.class);
	public static final MatcherPattern NUMBER = new MatcherPattern("\\d+", x -> {
		try {
			return Integer.parseInt(x);
		} catch (NumberFormatException e) {
		}
		return null;
	}, Integer.class);
	public static final MatcherPattern DIGIT = new MatcherPattern("\\d", x -> {
		try {
			Integer i = Integer.parseInt(x);
			if (i >= 0 && i <= 10)
				return i;
		} catch (NumberFormatException e) {
		}
		return null;
	}, Integer.class);
	public static final MatcherPattern HOUR = new MatcherPattern("[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");

}
