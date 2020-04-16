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
	private static Pattern matchEmail = Pattern.compile("^(.+)@(.+)\\.(.+)$");

	public static String getDate(String date) {
		return matchDate.matcher(date).group();
	}

	public static String getDouble(String i) {
		return matchDouble.matcher(i).group();
	}

	public static String getDuration(String duration) {
		return matchDuration.matcher(duration).group();
	}

	public static String getFakeIP(String ip) {
		return matchFakeIp.matcher(ip).group();
	}

	public static String getFakeUUID(String uuid) {
		return matchFakeUuid.matcher(uuid).group();
	}

	public static String getHour(String hour) {
		return matchHour.matcher(hour).group();
	}

	public static String getInt(String i) {
		return matchInt.matcher(i).group();
	}

	public static String getIP(String ip) {
		return matchIp.matcher(ip).group();
	}

	public static String getUsername(String username) {
		return matchUsername.matcher(username).group();
	}

	public static String getUUID(String uuid) {
		return matchUuid.matcher(uuid).group();
	}

	public static boolean isDate(String date) {
		return matchDate.matcher(date).find();
	}

	public static boolean isDouble(String i) {
		return matchDouble.matcher(i).find();
	}

	public static boolean isDuration(String duration) {
		return matchDuration.matcher(duration).find();
	}

	public static boolean isEmail(String email) {
		return matchEmail.matcher(email).find();
	}

	public static boolean isFakeIP(String ip) {
		return matchFakeIp.matcher(ip).find();
	}

	public static boolean isFakeUUID(String uuid) {
		return matchFakeUuid.matcher(uuid).find();
	}

	public static boolean isHour(String hour) {
		return matchHour.matcher(hour).find();
	}

	public static boolean isInt(String i) {
		return matchInt.matcher(i).find();
	}

	public static boolean isIP(String ip) {
		return matchIp.matcher(ip).find();
	}

	public static boolean isUsername(String username) {
		return matchUsername.matcher(username).find();
	}

	public static boolean isUUID(String uuid) {
		return matchUuid.matcher(uuid).find();
	}
}
