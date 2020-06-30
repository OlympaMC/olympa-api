package fr.olympa.api.scoreboard.sign;

import java.security.SecureRandom;
import java.util.Random;

public class SbUtils {
	private static final Random RANDOM = new SecureRandom();
	
	public static String generateRandomPassword(int length) {
		StringBuilder sb = new StringBuilder(length);
		for (int i = 0; i < length; i++) {
			int c = RANDOM.nextInt(62);
			if (c <= 9)
				sb.append(String.valueOf(c));
			else if (c < 36)
				sb.append((char) ('a' + c - 10));
			else
				sb.append((char) ('A' + c - 36));
		}
		return sb.toString();
	}
}
