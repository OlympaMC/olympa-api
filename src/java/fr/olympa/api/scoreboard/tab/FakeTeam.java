package fr.olympa.api.scoreboard.tab;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bukkit.entity.Player;

public class FakeTeam {

	//	private static long MAX_ID = 99_999_999_999L;
	private static final long MAX_ID = 10_000_000_000L;
	private static long nextId = 1L;
	private static final List<Long> ALL_IDS = new ArrayList<>();
	private static boolean overflow = false;

	public static void removeId(FakeTeam team) {
		ALL_IDS.remove(team.getId());
	}

	private final String name;
	private final Set<String> members = new HashSet<>();
	private final Set<Player> viewers = new HashSet<>();
	private long id;
	private String prefix = "";
	private String suffix = "";

	public long getId() {
		return id;
	}

	public FakeTeam(String prefix, String suffix, int sortPriority) {
		this("_" + getNameFromInput(sortPriority) + nextId, prefix, suffix);
	}

	//	public FakeTeam(String prefix, String suffix, int sortPriority, int secondPriority) {
	//		this("T_" + getNameFromInput(sortPriority) + getNameFromInput(secondPriority) + ID, prefix, suffix);
	//	}

	private FakeTeam(String name, String prefix, String suffix) {
		id = nextId;
		ALL_IDS.add(nextId);
		if (nextId >= MAX_ID)
			overflow = true;
		if (overflow) {
			for (long i = 1L; i < MAX_ID; i++)
				if (!ALL_IDS.contains(i))
					nextId = i;
		} else
			nextId++;
		this.name = name.length() > 16 ? name.substring(0, 16) : name;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public boolean isValidTeam() {
		return name != null && name.length() <= 16 && prefix != null && suffix != null && members != null && id <= MAX_ID;
	}

	public void addMember(String player) {
		members.add(player);
	}

	public void removeMember(String player) {
		members.remove(player);
	}

	public void addViewer(Player player) {
		viewers.add(player);
	}

	public void addViewers(Collection<Player> players) {
		viewers.addAll(players);
	}

	public void removeViewer(Player player) {
		viewers.remove(player);
	}

	public void removeViewers(Collection<Player> player) {
		viewers.removeAll(player);
	}

	public Set<Player> getViewers() {
		return viewers;
	}

	public Set<String> getMembers() {
		return members;
	}

	public String getName() {
		return name;
	}

	/**
	 * This is a special method to sort nametags in the tablist. It takes a priority
	 * and converts it to an alphabetic representation to force a specific sort.
	 *
	 * @param input the sort priority
	 * @return the team name
	 */
	private static String getNameFromInputOld(int input) {
		if (input < 0)
			return "Z";
		char letter = (char) (input / 5 + 65);
		int repeat = input % 5 + 1;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < repeat; i++)
			builder.append(letter);
		return builder.toString();
	}

	private static String getNameFromInput2(int input) {
		String sorting = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		StringBuilder builder = new StringBuilder();
		while (input > 0)
			if (input >= sorting.length()) {
				builder.append(sorting.charAt(sorting.length() - 1));
				input -= sorting.length();
			} else
				builder.append(sorting.charAt(input));
		return builder.toString();
	}

	private static String getNameFromInput(int input) {
		if (input < 0 || input > 250)
			return "z";
		char letter = (char) (input / 5 + 'A');
		if (letter > 'Z')
			letter += 6;
		int repeat = input % 5 + 1;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < repeat; i++)
			builder.append(letter);
		return builder.toString();
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean isSimilar(String prefix, String suffix) {
		return this.prefix.equals(prefix) && this.suffix.equals(suffix);
	}

}
