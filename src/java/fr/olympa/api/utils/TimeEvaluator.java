package fr.olympa.api.utils;

import fr.olympa.api.LinkSpigotBungee;

public class TimeEvaluator {

	String name;
	long time;
	long timeTaken;

	private static long getNanoTime() {
		return System.nanoTime();
	}

	public TimeEvaluator(String name) {
		this.name = name;
		time = getNanoTime();
	}

	public long stop() {
		return timeTaken = getNanoTime() - time;
	}

	public void print() {
		long timeTaken2 = getTimeDiff();
		if (timeTaken == 0)
			timeTaken2 = stop();
		LinkSpigotBungee.Provider.link.sendMessage("&6[TIME EVALUATOR] &e%s took %d nanoseconds (or %s miliseconds)", name, timeTaken2, timeTaken2 / 1000000D);
	}

	public long getTimeDiff() {
		return timeTaken;
	}
}
