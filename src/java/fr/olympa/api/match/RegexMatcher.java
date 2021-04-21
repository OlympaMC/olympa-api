package fr.olympa.api.match;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

public class RegexMatcher {

	public static final MatcherPattern<String> NOT_LETTER = new MatcherPattern<>("[^\\w]");
	public static final MatcherPattern<String> USERNAME = new MatcherPattern<>("[\\w_]{3,16}");
	public static final MatcherPattern<String> IP = new MatcherPattern<>("((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}");

	public static final MatcherPattern<String> FAKE_IP = new MatcherPattern<>("\\d{1,3}(\\.\\d{1,3}){3}");
	public static final MatcherPattern<String> FAKE_UUID = new MatcherPattern<>("[0-9a-z]{8}-([0-9a-z]{4}-){3}[0-9a-z]{12}");
	public static final MatcherPattern<String> EMAIL = new MatcherPattern<>("(.+)@(.+)\\.(.+)");
	public static final MatcherPattern<String> DISCORD_TAG = new MatcherPattern<>(".{2,32}#[0-9]{4}");

	public static final MatcherPattern<UUID> UUID = new MatcherPattern<>("[0-9a-f]{8}-?([0-9a-f]{4}-?){3}[0-9a-f]{12}", x -> {
		try {
			return java.util.UUID.fromString(x.contains("-") ? x : x.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}, UUID.class);

	public static final MatcherPattern<LocalDate> DATE = new MatcherPattern<>("[0-9]{1,4}/[0-9]{1,2}/[0-9]{1,2}", x -> {
		try {
			return LocalDate.parse(x, DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		} catch (DateTimeParseException e2) {
			return null;
		}
	});
	public static final MatcherPattern<Double> DOUBLE = new MatcherPattern<>("-?\\d+(.\\d+)?", x -> {
		try {
			return Double.parseDouble(x.replace(",", "."));
		} catch (NumberFormatException e) {
			return null;
		}
	});
	public static final MatcherPattern<Double> RELATIVE = new MatcherPattern<>("~-?(\\d+(.\\d+)?)?", x -> {
		if (x.equals("~"))
			return 0d;
		x = x.substring(1);
		try {
			return Double.parseDouble(x.replace(",", "."));
		} catch (NumberFormatException e) {
			return null;
		}
	});
	public static final MatcherPattern<Float> FLOAT = new MatcherPattern<>("-?\\d+(.\\d+)?", x -> {
		try {
			return Float.parseFloat(x.replace(",", "."));
		} catch (NumberFormatException e) {
			return null;
		}
	});
	public static final MatcherPattern<Integer> INT = new MatcherPattern<>("-?\\d{0,10}", x -> {
		try {
			return Integer.parseInt(x);
		} catch (NumberFormatException e) {
			return null;
		}
	}, Integer.class);
	public static final MatcherPattern<Long> LONG = new MatcherPattern<>("-?\\d{0,19}", x -> {
		try {
			return Long.parseLong(x);
		} catch (NumberFormatException e) {
			return null;
		}
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
	public static final MatcherPattern<Integer> NUMBER = new MatcherPattern<>("\\d+", x -> {
		try {
			return Integer.parseInt(x);
		} catch (NumberFormatException e) {}
		return null;
	}, Integer.class);
	public static final MatcherPattern<Integer> DIGIT = new MatcherPattern<>("\\d", x -> {
		try {
			Integer i = Integer.parseInt(x);
			if (i >= 0 && i <= 10)
				return i;
		} catch (NumberFormatException e) {}
		return null;
	}, Integer.class);
	public static final MatcherPattern<String> HEX_COLOR = new MatcherPattern<>("[xX#]?[0-9a-fA-F]{6}", x -> {
		char c = x.charAt(0);
		if (c == 'x' || c == 'X' || c == '#')
			x = x.substring(1);
		return "#" + x;
	}, String.class);
	public static final MatcherPattern<String> HOUR = new MatcherPattern<>("[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");

	private RegexMatcher() {

	}
}
