package fr.olympa.api.match;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegexMatcher {

	public static MatcherPattern USERNAME = new MatcherPattern("(?iu)^[\\w_]{3,16}$");
	public static MatcherPattern IP = new MatcherPattern("((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}");

	public static MatcherPattern UUID = new MatcherPattern("[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}",
			x -> java.util.UUID.fromString(x.contains("-") ? x : x.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), java.util.UUID.class);

	public static MatcherPattern FAKE_IP = new MatcherPattern("\\d{1,3}(\\.\\d{1,3}){3}");
	public static MatcherPattern FAKE_UUID = new MatcherPattern("[0-9a-z]{8}-([0-9a-z]{4}-){3}[0-9a-z]{12}");
	public static MatcherPattern EMAIL = new MatcherPattern("(.+)@(.+)\\.(.+)");
	public static MatcherPattern DISCORD_TAG = new MatcherPattern(".*#[0-9]{4}");

	public static MatcherPattern DATE = new MatcherPattern("[0-9]{1,4}/[0-9]{1,2}/[0-9]{1,2}", x -> LocalDate.parse(x, DateTimeFormatter.ofPattern("dd/MM/yyyy")));
	public static MatcherPattern DOUBLE = new MatcherPattern("-?\\d+(.\\d+)?");
	public static MatcherPattern INT = new MatcherPattern("-?\\d+", x -> Integer.parseInt(x), Integer.class);
	public static MatcherPattern NUMBER = new MatcherPattern("\\d+", x -> Integer.parseInt(x), Integer.class);
	public static MatcherPattern DIGIT = new MatcherPattern("\\d", x -> {
		Integer i = Integer.parseInt(x);
		if (i >= 0 && i <= 10)
			return i;
		return null;
	}, Integer.class);
	public static MatcherPattern HOUR = new MatcherPattern("[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");
}
