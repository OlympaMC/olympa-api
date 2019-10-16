package fr.olympa.api.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.text.Collator;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import net.md_5.bungee.api.chat.TextComponent;

public class Utils {

	private static final Collector<?, ?, ?> SHUFFLER = Collectors.collectingAndThen(Collectors.toList(), list -> {
		Collections.shuffle(list);
		return list;
	});

	public static long addTimeToCurrentTime(final int field, final int amount) {
		final Calendar calendar = Calendar.getInstance();
		calendar.add(field, amount);
		return calendar.getTimeInMillis() / 1000;
	}

	public static int booleanToBinary(final Boolean b) {
		return b.booleanValue() ? 1 : 0;
	}

	public static String capitalize(final String name) {
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}

	/**
	 * Permet de colorier chaque lettre une à une dans un mot pour faire une
	 * animation Pour BungeeCord
	 */
	public static List<String> colorString(final String string, final net.md_5.bungee.api.ChatColor color1, final net.md_5.bungee.api.ChatColor color2) {
		final List<String> dyn = new ArrayList<>();
		for (int i = 0; i < string.length(); i++) {
			dyn.add(color1 + string.substring(0, i) + color2 + string.substring(i, i + 1) + color1 + string.substring(i + 1, string.length()));
		}
		dyn.add(color1 + string);
		return dyn;
	}

	/**
	 * Permet de colorier chaque lettre une à une dans un mot pour faire une
	 * animation Pour Spigot
	 */
	public static List<String> colorString(final String string, final org.bukkit.ChatColor color1, final org.bukkit.ChatColor color2) {
		final List<String> dyn = new ArrayList<>();
		for (int i = 0; i < string.length(); i++) {
			dyn.add(color1 + string.substring(0, i) + color2 + string.substring(i, i + 1) + color1 + string.substring(i + 1, string.length()));

		}
		dyn.add(color1 + string);
		return dyn;
	}

	/**
	 * Permet de savoir si un String ignore case est dans une List<String>
	 *
	 * @param s String
	 * @param l List<String>
	 * @return Boolean true/false
	 */

	public static boolean containsIgnoreCase(final String s, final List<String> l) {
		return l.stream().filter(Ls -> Ls.equalsIgnoreCase(s)).findFirst().isPresent();
	}

	public static void copyFile(final File source, final File dest) throws IOException {
		InputStream is = null;
		OutputStream os = null;
		try {
			is = new FileInputStream(source);
			os = new FileOutputStream(dest);
			final byte[] buffer = new byte[1024];
			int length;
			while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			}
		} finally {
			is.close();
			os.close();
		}
	}

	public static boolean equalsIgnoreAccents(final String a, final String b) {
		final Collator insenstiveStringComparator = Collator.getInstance();
		insenstiveStringComparator.setStrength(Collator.PRIMARY);
		return insenstiveStringComparator.compare(a, b) == 0;
	}

	public static boolean equalsIgnoreCase(String text, String text2) {
		return removeAccents(text.toLowerCase()).equalsIgnoreCase(removeAccents(text2.toLowerCase()));
	}

	/**
	 * Récupère le String après un caratère dans un String
	 *
	 * @param s     String
	 * @param after Caratère contenu dans s
	 */
	public static String getAfterFirst(final String s, final String after) {
		return s.substring(s.indexOf(after) + 1).trim();
	}

	public static long getCurrentTimeinSeconds() {
		return System.currentTimeMillis() / 1000L;
	}

	/**
	 * Récupère un String ignore case dans une List<String>
	 *
	 * @param s String
	 * @param l List<String>
	 * @return List<String>
	 */
	public static List<String> getIgnoreCase(final String s, final List<String> l) {
		return l.stream().filter(Ls -> Ls.equalsIgnoreCase(s)).collect(Collectors.toList());
	}

	public static JsonElement getJsonObject(final String url) {
		try {
			JsonElement obj;
			final URL url2 = new URL(url);
			final InputStreamReader reader = new InputStreamReader(url2.openStream());
			obj = new JsonParser().parse(reader);
			return obj;
		} catch (final IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Permet de récupérer la lettre correspondant au nombre
	 *
	 * @param i 0 < i < 27
	 */
	public static String getLetterOfNumber(final int i) {
		return i > 0 && i < 27 ? String.valueOf((char) (i + 64 + 32)) : null;
	}

	/**
	 * Récupère le nombre de miliseconde avant la prochaine minute
	 *
	 * @return temps en milliseconds
	 */
	public static long getMillisBeforeNextMin() {
		final Calendar calendar = Calendar.getInstance();
		final long startTime = calendar.getTimeInMillis();
		calendar.add(Calendar.MINUTE, 1);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTimeInMillis() - startTime;
	}

	/**
	 * Permet de récupérer la lettre opposé correspondant au nombre
	 *
	 * @param i 0 < i < 27
	 */
	public static String getOppositeCharForNumber(final int i) {
		return i > 0 && i < 27 ? String.valueOf((char) ('Z' - (Character.toUpperCase((char) (i + 64)) - 'A'))) : null;
	}

	public static Object getRandom(final List<?> list) {
		return list.get(new Random().nextInt(list.size()));
	}

	public static String insertChar(final String string, final String insert) {
		final StringBuilder builder = new StringBuilder(string);
		int index = 0;
		for (int i = 0; i < string.length() + 1; i++) {
			builder.insert(index, insert);
			index += 2;
		}
		return builder.toString();
	}

	public static String intToSymbole(final int i) {
		return String.valueOf(i)
				.replaceAll("10", "➓")
				.replaceAll("0", "")
				.replaceAll("1", "➊")
				.replaceAll("2", "➋")
				.replaceAll("3", "➌")
				.replaceAll("4", "➍")
				.replaceAll("5", "➎")
				.replaceAll("6", "➏")
				.replaceAll("7", "➐")
				.replaceAll("8", "➑")
				.replaceAll("9", "➒");
	}

	public static boolean isJSONValid(final String jsonInString) {
		try {
			new Gson().fromJson(jsonInString, Object.class);
			return true;
		} catch (final JsonSyntaxException ex) {
			return false;
		}
	}

	public static float nextFloat(final float min, final float max) {
		return nextFloat(new Random(), min, max);
	}

	public static float nextFloat(final Random random, final float min, final float max) {
		return min + random.nextFloat() * (max - min);
	}

	public static String removeAccents(String text) {
		return text == null ? null
				: Normalizer.normalize(text, Form.NFD)
						.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}

	public static List<String> replaceInList(List<String> list, final HashMap<String, String> replace) {
		for (final Entry<String, String> Ereplace : replace.entrySet()) {
			list = replaceInList(list, Ereplace.getKey(), Ereplace.getValue());
		}
		return list;
	}

	public static List<String> replaceInList(final List<String> list, final String toReplace, final String replaced) {
		final List<String> list2 = new ArrayList<>(list);
		for (int i = 0; i < list2.size(); i++) {
			list2.set(i, list2.get(i).replaceAll(toReplace, replaced));
		}
		return list2;
	}

	/**
	 * Permet d'arrondir un double avec x nombre après la virgule
	 */
	public static double round(double value, final int x) {
		if (x < 0) {
			throw new IllegalArgumentException();
		}

		final long factor = (long) Math.pow(10, x);
		value = value * factor;
		final long tmp = Math.round(value);
		return (double) tmp / factor;
	}

	public static String secondsToCalendar(final int s) {
		return LocalTime.ofSecondOfDay(s).toString();
	}

	public static List<String> startWords(String word, Set<String> allWords) {
		TreeSet<String> startWordList = new TreeSet<>(Collator.getInstance());
		for (String currentWord : allWords) {
			if (removeAccents(currentWord).toLowerCase().startsWith(removeAccents(word.toLowerCase()))) {
				startWordList.add(currentWord);
			}
		}
		return new ArrayList<>(startWordList);
	}

	public static String timestampToDate(final long timestamp) {
		final SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(new Date(timestamp * 1000));
	}

	public static String timestampToDateAndHour(final long timestamp) {
		final SimpleDateFormat format = new SimpleDateFormat("HH:mm dd/MM/yyyy");
		format.setTimeZone(TimeZone.getTimeZone("GMT"));
		return format.format(new Date(timestamp * 1000));
	}

	public static String timestampToDuration(final long timestamp) {

		final long now = Utils.getCurrentTimeinSeconds();
		final LocalDateTime timestamp2 = LocalDateTime.ofInstant(Instant.ofEpochSecond(timestamp), TimeZone.getDefault().toZoneId());
		final LocalDateTime now2 = LocalDateTime.ofInstant(Instant.ofEpochSecond(now), TimeZone.getDefault().toZoneId());

		LocalDateTime start;
		LocalDateTime end;
		if (timestamp > now) {
			start = now2;
			end = timestamp2;
		} else {
			start = timestamp2;
			end = now2;
		}

		Duration dur = Duration.between(start.toLocalTime(), end.toLocalTime());
		LocalDate e = end.toLocalDate();

		if (dur.isNegative()) {
			dur = dur.plusDays(1);
			e = e.minusDays(1);
		}
		final Period per = Period.between(start.toLocalDate(), e);

		final long year = per.getYears();
		final long month = per.getMonths();
		final long day = per.getDays();
		final long hour = dur.toHours();
		final long minute = dur.toMinutes() - 60 * dur.toHours();
		final long second = dur.getSeconds() - 60 * dur.toMinutes();

		final List<String> msg = new ArrayList<>();
		if (year > 1) {
			msg.add(year + " ans");
		} else if (year == 1) {
			msg.add(year + " an");
		}
		if (month != 0) {
			msg.add(month + " mois");
		}

		if (day > 1) {
			msg.add(day + " jours");
		} else if (day == 1) {
			msg.add(day + " jour");
		}
		if (hour > 1) {
			msg.add(hour + " heures");
		} else if (hour == 1) {
			msg.add(hour + " heure");
		}
		if (minute > 1) {
			msg.add(minute + " minutes");
		} else if (minute == 1) {
			msg.add(minute + " minute");
		}
		if (second > 1) {
			msg.add(second + " secondes");
		} else if (second == 1) {
			msg.add(second + " seconde");
		}
		// System.out.println("Timestamp " + timestamp + " " + year + "year " + month +
		// "month " + day + "day " + hour + "hour " + minute + "minute " + second +
		// "second ");

		final List<String> msgs = new ArrayList<>();
		for (final String message : msg) {
			if (message != null) {
				msgs.add(message);
			}
			if (msgs.size() >= 2) {
				break;
			}
		}
		return String.join(", ", msgs);
	}

	public static int toField(final String field) {
		switch (field) {

		case "year":
			return Calendar.YEAR;
		case "month":
		case "mois":
			return Calendar.MONTH;
		case "day":
			return Calendar.DAY_OF_MONTH;
		case "hour":
			return Calendar.HOUR_OF_DAY;
		case "minute":
			return Calendar.MINUTE;
		case "second":
			return Calendar.SECOND;
		default:
			return 0;
		}

	}

	@SuppressWarnings("unchecked")
	public static <T> Collector<T, ?, List<T>> toShuffledList() {
		return (Collector<T, ?, List<T>>) SHUFFLER;
	}

	public static void toTextComponent(final TextComponent msg, final List<TextComponent> list, final String separator, final String end) {
		if (list.size() == 0) {
			return;
		}
		int i = 1;
		for (final TextComponent targetAccountsText : list) {
			msg.addExtra(targetAccountsText);
			if (i == list.size()) {
				targetAccountsText.addExtra(end);
			} else {
				targetAccountsText.addExtra(separator);
			}
			i++;
		}
	}

	/**
	 * Permet d'accroder ou nom un mot.
	 *
	 * @param i Nombre d'object
	 * @return Un s si le nombre est supérieur à 1, sinon un string vide.
	 */
	public static String withOrWithoutS(final int i) {
		return i < 2 ? "" : "s";
	}
}