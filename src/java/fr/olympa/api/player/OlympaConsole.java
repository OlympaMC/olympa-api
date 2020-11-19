package fr.olympa.api.player;

import java.util.UUID;

public class OlympaConsole {

	final private static String name = "Console";
	final private static long id = 0;
	final private static UUID uuid = UUID.fromString("17fbd2b2-1f24-36ab-9bde-7be8f242ad60");
	final private static UUID uuidPremium = UUID.fromString("f78a4d8d-d51b-4b39-98a3-230f2de0c670");
	private static OlympaPlayer devConnected = null;

	public static void delConnected(OlympaPlayer devConnected) {
		OlympaConsole.devConnected = null;
	}

	public static long getId() {
		return id;
	}

	public static UUID getUuid() {
		return uuid;
	}

	public static UUID getUuidpremium() {
		return uuidPremium;
	}

	public static OlympaPlayer getDevConnected() {
		return devConnected;
	}

	/**
	 * @return the name
	 */
	public static String getName() {
		return name;
	}

	/**
	 * @return the uuid
	 */
	public static UUID getUniqueId() {
		return uuid;
	}

	public static boolean isConsole(String name2) {
		return name.equalsIgnoreCase(name2);
	}

	public static boolean isConsole(UUID uuid2) {
		return uuid == uuid2;
	}

	public static void setDevConnected(OlympaPlayer devConnected) {
		OlympaConsole.devConnected = devConnected;
	}
}
