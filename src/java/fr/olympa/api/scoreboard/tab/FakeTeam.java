package fr.olympa.api.scoreboard.tab;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FakeTeam {

	private static long MAX_ID = 99_999_999_999L;
	private static long ID = 0L;
	private static List<Long> ALL_IDS = new ArrayList<>();
	private static boolean overflow = false;

	public static void removeId(FakeTeam team) {
		ALL_IDS.remove(team.getId());
	}

	private final Set<String> members = new HashSet<>();
	private String name;
	private long id;
	private String prefix = "";
	private String suffix = "";

	public long getId() {
		return id;
	}

	public FakeTeam(String prefix, String suffix, int sortPriority) {
		this("T_" + getNameFromInput(sortPriority) + ID, prefix, suffix);
	}

	public FakeTeam(String prefix, String suffix, int sortPriority, int secondPriority) {
		this("T_" + getNameFromInput(sortPriority) + getNameFromInput(secondPriority) + ID, prefix, suffix);
	}

	private FakeTeam(String name, String prefix, String suffix) {
		id = ID;
		ALL_IDS.add(ID);
		if (ID >= MAX_ID)
			overflow = true;
		if (overflow) {
			for (long i = 0L; i < MAX_ID; i++)
				if (!ALL_IDS.contains(i))
					ID = i;
		} else
			ID++;

		name = name.length() > 16 ? name.substring(0, 16) : name;
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public void addMember(String player) {
		members.add(player);
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
	private static String getNameFromInput(int input) {
		if (input < 0)
			return "Z";
		char letter = (char) (input / 5 + 65);
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
