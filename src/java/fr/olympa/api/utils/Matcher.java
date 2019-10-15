package fr.olympa.api.utils;

import java.util.regex.Pattern;

public class Matcher {

	private static Pattern matchDate = Pattern.compile("[0-9]{1,4}/[0-9]{1,2}/[0-9]{1,2}");
	private static Pattern matchDouble = Pattern.compile("^-?\\d+(.\\d+)?$");
	private static Pattern matchDuration = Pattern.compile("\\b[0-9]+");
	private static Pattern matchFakeIp = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}$");
	private static Pattern matchFakeUuid = Pattern.compile("^[0-9a-z]{8}-([0-9a-z]{4}-){3}[0-9a-z]{12}$");
	private static Pattern matchHour = Pattern.compile("[0-9]{1,2}:[0-9]{1,2}:[0-9]{1,2}");
	private static Pattern matchInt = Pattern.compile("^-?\\d+$");
	private static Pattern matchIp = Pattern.compile("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}$");
	private static Pattern matchUsername = Pattern.compile("(?iu)^[a-z0-9_-]{3,16}$");
	private static Pattern matchUuid = Pattern.compile("^[0-9a-f]{8}-([0-9a-f]{4}-){3}[0-9a-f]{12}$");

	public static String getDate(final String date) {
		return matchDate.matcher(date).group();
	}

	public static String getDouble(final String i) {
		return matchDouble.matcher(i).group();
	}

	public static String getDuration(final String duration) {
		return matchDuration.matcher(duration).group();
	}

	public static String getFakeIP(final String ip) {
		return matchFakeIp.matcher(ip).group();
	}

	public static String getFakeUUID(final String uuid) {
		return matchFakeUuid.matcher(uuid).group();
	}

	public static String getHour(final String hour) {
		return matchHour.matcher(hour).group();
	}

	public static String getInt(final String i) {
		return matchFakeIp.matcher(i).group();
	}

	public static String getIP(final String ip) {
		return matchIp.matcher(ip).group();
	}

	public static String getUsername(final String username) {
		return matchUsername.matcher(username).group();
	}

	public static String getUUID(final String uuid) {
		return matchUuid.matcher(uuid).group();
	}

	public static boolean isDate(final String date) {
		return matchDate.matcher(date).find();
	}

	public static boolean isDouble(final String i) {
		return matchDouble.matcher(i).find();
	}

	public static boolean isDuration(final String duration) {
		return matchDuration.matcher(duration).find();
	}

	public static boolean isFakeIP(final String ip) {
		return matchFakeIp.matcher(ip).find();
	}

	public static boolean isFakeUUID(final String uuid) {
		return matchFakeUuid.matcher(uuid).find();
	}

	public static boolean isHour(final String hour) {
		return matchHour.matcher(hour).find();
	}

	public static boolean isInt(final String i) {
		return matchInt.matcher(i).find();
	}

	public static boolean isIP(final String ip) {
		return matchIp.matcher(ip).find();
	}

	public static boolean isUsername(final String username) {
		return matchUsername.matcher(username).find();
	}

	public static boolean isUUID(final String uuid) {
		return matchUuid.matcher(uuid).find();
	}
}
