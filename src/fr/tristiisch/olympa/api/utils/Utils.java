package fr.tristiisch.olympa.api.utils;

import java.text.Normalizer;
import java.text.Normalizer.Form;

public class Utils {

	public static boolean equalsIgnoreCase(String text, String text2) {
		return removeAccents(text.toLowerCase()).equalsIgnoreCase(removeAccents(text2.toLowerCase()));
	}

	public static String removeAccents(String text) {
		return text == null ? null
				: Normalizer.normalize(text, Form.NFD)
						.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
	}
}
