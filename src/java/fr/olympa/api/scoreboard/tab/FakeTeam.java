package fr.olympa.api.scoreboard.tab;

import java.util.ArrayList;

import fr.olympa.api.utils.ProtocolAPI;
import fr.olympa.api.utils.Utils;

/**
 * This class represents a Scoreboard Team. It is used to keep track of the
 * current members of a Team, and is responsible for
 */
public class FakeTeam {

	// Because some networks use NametagEdit on multiple servers, we may have
	// clashes
	// with the same Team names. The UNIQUE_ID ensures there will be no clashing.
	private static final String UNIQUE_ID = Utils.generateUUID();
	// This represents the number of FakeTeams that have been created.
	// It is used to generate a unique Team name.
	private static int ID = 0;

	private final ArrayList<String> members = new ArrayList<>();
	private String name;
	private String prefix = "";
	private String suffix = "";

	public FakeTeam(String prefix, String suffix, int sortPriority, boolean playerTag) {
		name = UNIQUE_ID + "_" + getNameFromInput(sortPriority) + ++ID + (playerTag ? "+P" : "");
		if (ProtocolAPI.V1_13.isNewerThanDefault()) {
			name = name.length() > 128 ? name.substring(0, 128) : name;
		} else {
			name = name.length() > 16 ? name.substring(0, 16) : name;
		}
		this.prefix = prefix;
		this.suffix = suffix;
	}

	public void addMember(String player) {
		if (!members.contains(player)) {
			members.add(player);
		}
	}

	public ArrayList<String> getMembers() {
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
	private String getNameFromInput(int input) {
		if (input < 0) {
			return "Z";
		}
		char letter = (char) (input / 5 + 65);
		int repeat = input % 5 + 1;
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < repeat; i++) {
			builder.append(letter);
		}
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
