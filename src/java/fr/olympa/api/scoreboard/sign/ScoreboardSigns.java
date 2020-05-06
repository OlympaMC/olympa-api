package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Passwords;
import fr.olympa.api.utils.Reflection;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.IScoreboardCriteria.EnumScoreboardHealthDisplay;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_15_R1.ScoreboardServer.Action;

/**
 * A simple tool to manage scoreboards in minecraft (lines up to 48 characters !).<br>
 * Edited by me to permit more flexibility
 * @see <a href="https://gist.github.com/zyuiop/8fcf2ca47794b92d7caa">Original file on GitHub</a>
 * @author zyuiop, SkytAsul
 */
public class ScoreboardSigns implements Cloneable {

	private boolean created;

	private final ArrayList<VirtualTeam> lines = new ArrayList<>();
	private final Player player;
	protected String objectiveName;
	private int maxSize = 0;
	private String displayName;

	/**
	 * Create a scoreboard sign for a given player and using a specifig objective name
	 * @param player the player viewing the scoreboard sign
	 * @param objectiveName the name of the scoreboard sign (displayed at the top of the scoreboard)
	 */
	public ScoreboardSigns(Player player, String displayName, String objectiveName, int maxSize) {
		this.player = player;
		this.displayName = displayName;
		this.objectiveName = objectiveName.length() > 16 ? objectiveName.substring(0, 16) : objectiveName;
		this.maxSize = maxSize;
	}

	/**
	 * Change the name of the objective. The name is displayed at the top of the scoreboard.
	 * @param name the name of the objective, max 32 char
	 * @throws ClassNotFoundException reflection problem
	 */
	public void changeDisplayName(String name) throws ClassNotFoundException {
		objectiveName = name;
		if (created) {
			Reflection.sendPacket(player, createObjectivePacket(2));
		}
	}

	@Override
	public ScoreboardSigns clone() {
		try {
			return (ScoreboardSigns) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Check if a line has the exact same value
	 * @param value line value to check
	 * @return true if a line has the same value
	 */
	public boolean containsValue(String value, VirtualTeam except) {
		for (VirtualTeam team : lines) {
			// if (team != null) System.out.println("VALUE: " + value + " | TEAM: " +
			// team.getValue() + " | SAME: " + (except == team));
			if (team != null && team != except && value.equals(team.getValue())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Send the initial creation packets for this scoreboard sign. Must be called at least once.
	 */
	public void create() {
		if (created) {
			return;
		}
		Reflection.sendPacket(player, createObjectivePacket(0));
		created = true;
	}

	/*
	 * Factories
	 */
	private Object createObjectivePacket(int mode) {
		Object packet = new PacketPlayOutScoreboardObjective();
		Reflection.setField(packet, "a", objectiveName);
		// Mode
		// 0 : créer
		// 1 : Supprimer
		// 2 : Mettre à jour
		Reflection.setField(packet, "d", mode);

		if (mode == 0 || mode == 2) {
			Reflection.setField(packet, "b", new ChatComponentText(displayName));
			Reflection.setField(packet, "c", EnumScoreboardHealthDisplay.INTEGER);
		}
		return packet;
	}

	/**
	 * Send the packets to remove this scoreboard sign and remove teams. A destroyed scoreboard sign must be recreated using create() in order
	 * to be used again.
	 */
	public void destroy() {
		if (!created) {
			Reflection.sendPacket(player, createObjectivePacket(1));
		}
		for (VirtualTeam team : lines) {
			if (team != null) {
				Reflection.sendPacket(player, team.removeTeam());
			}
		}
		created = false;
	}

	/**
	 * Display Scoreboard in objective slot
	 */
	public void display() {
		if (!created) {
			return;
		}
		Reflection.sendPacket(player, setObjectiveSlot());
	}

	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Get the current value for a line
	 * @param line the line
	 * @return the content of the line
	 */
	public String getLine(int line) {
		if (line > 14) {
			return null;
		}
		if (line < 0) {
			return null;
		}
		return getOrCreateTeam(line).getValue();
	}

	private VirtualTeam getOrCreateTeam(int line) {
		String teamName = "__" + Passwords.generateRandomPassword(16 - 2);
		if (lines.size() <= line) {
			lines.add(new VirtualTeam(teamName));
			if (lines.size() > maxSize) {
				maxSize = lines.size();
			}
		} else if (lines.get(line) == null) {
			lines.set(line, new VirtualTeam(teamName));
		}

		return lines.get(line);
	}

	private int getScore(int i) {
		return maxSize - i;
	}

	/**
	 * Get the team assigned to a line
	 * @param line line number
	 * @return the VirtualTeam used to display this line
	 */
	public VirtualTeam getTeam(int line) {
		if (line > 14) {
			return null;
		}
		if (line < 0) {
			return null;
		}
		return getOrCreateTeam(line);
	}

	/**
	 * Get the line assigned to a team
	 * @param team Team object to get index of
	 * @return the line number assigned to the specified team
	 */
	public int getTeamLine(VirtualTeam team) {
		return lines.indexOf(team);
	}

	public void moveLines(int start, int amount) {
		int newSize = lines.size() + amount;
		for (int i = start; i < newSize; i++) { // from the start line to the end of the final list
			if (i < start + amount) { // insert null values to make space
				lines.add(start, null);
			} else { // refresh scores of the next lines
				VirtualTeam val = getOrCreateTeam(i);
				Reflection.sendPacket(player, sendScore(val.getCurrentPlayer(), getScore(i)));
			}
		}
	}

	/**
	 * Remove a given scoreboard line
	 * @param line the line to remove
	 */
	public void removeLine(int line) {
		VirtualTeam team = getOrCreateTeam(line);
		String old = team.getCurrentPlayer();

		if (old != null && created) {
			Reflection.sendPacket(player, this.removeLine(old));
			Reflection.sendPacket(player, team.removeTeam());
		}

		lines.remove(line);
		for (int i = line; i < lines.size(); i++) {
			VirtualTeam val = getOrCreateTeam(i);
			Reflection.sendPacket(player, sendScore(val.getCurrentPlayer(), getScore(i)));
		}
	}

	private Object removeLine(String line) {
		Object packet = new PacketPlayOutScoreboardScore();
		Reflection.setField(packet, "a", line);
		Reflection.setField(packet, "d", Action.REMOVE);
		return packet;
	}

	private void sendLine(int line) throws ClassNotFoundException {
		if (line > 14) {
			return;
		}
		if (line < 0) {
			return;
		}
		if (!created) {
			return;
		}

		int score = getScore(line);
		VirtualTeam val = getOrCreateTeam(line);
		for (Object packet : val.sendLine()) {
			Reflection.sendPacket(player, packet);
		}
		Reflection.sendPacket(player, sendScore(val.getCurrentPlayer(), score));
		val.reset();
	}

	public void sendLines() {
		if (!created) {
			return;
		}
		try {
			int i = 0;
			while (i < lines.size()) {
				sendLine(i++);
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private Object sendScore(String line, int score) {
		Object packet = new PacketPlayOutScoreboardScore();
		Reflection.setField(packet, "a", line);
		Reflection.setField(packet, "b", objectiveName);
		Reflection.setField(packet, "c", score);
		Reflection.setField(packet, "d", Action.CHANGE);
		return packet;
	}

	/**
	 * Change a scoreboard line and send the packets to the player. Can be called async.
	 * @param line the number of the line (0 &#60;= line &#60; 15)
	 * @param value the new value for the scoreboard line
	 * @return VirtualTeam created or edited
	 */
	public VirtualTeam setLine(int line, String value) {
		try {
			VirtualTeam team = getOrCreateTeam(line);
			String old = team.getCurrentPlayer();

			if (old != null && created) {
				System.out.println("setLine Remove '" + old + "'");
				Reflection.sendPacket(player, this.removeLine(old));
			}

			while (containsValue(value, team)) {
				value = value + "§r"; // add a space if a line with the value already exists
			}
			team.setValue(value);
			sendLine(line);
			return team;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	private Object setObjectiveSlot() {
		Object packet = new PacketPlayOutScoreboardDisplayObjective();
		// Slot
		Reflection.setField(packet, "a", 1);
		Reflection.setField(packet, "b", objectiveName);

		return packet;
	}
}