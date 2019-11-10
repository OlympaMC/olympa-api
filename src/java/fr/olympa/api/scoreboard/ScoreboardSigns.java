package fr.olympa.api.scoreboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_13_R2.ChatComponentText;
import net.minecraft.server.v1_13_R2.IScoreboardCriteria.EnumScoreboardHealthDisplay;
import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_13_R2.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_13_R2.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_13_R2.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_13_R2.ScoreboardServer.Action;

/**
 * A simple tool to manage scoreboards in minecraft (lines up to 48 characters !).<br>
 * Edited by me to permit more flexibility
 * @see <a href="https://gist.github.com/zyuiop/8fcf2ca47794b92d7caa">Original file on GitHub</a>
 * @author zyuiop, SkytAsul
 */
public class ScoreboardSigns {

	private boolean created = false;
	private final ArrayList<VirtualTeam> lines = new ArrayList<>(15);
	private final Player player;
	private String objectiveName;

	/**
	 * Create a scoreboard sign for a given player and using a specifig objective name
	 * @param player the player viewing the scoreboard sign
	 * @param objectiveName the name of the scoreboard sign (displayed at the top of the scoreboard)
	 */
	public ScoreboardSigns(Player player, String objectiveName) {
		this.player = player;
		this.objectiveName = objectiveName;
	}

	/**
	 * Send the initial creation packets for this scoreboard sign. Must be called at least once.
	 */
	public void create() {
		if (created)
			return;

		try {
			sendPacket(player, createObjectivePacket(0, objectiveName));
			sendPacket(player, setObjectiveSlot());
			int i = 0;
			while (i < lines.size())
				sendLine(i++);

			created = true;
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Send the packets to remove this scoreboard sign. A destroyed scoreboard sign must be recreated using create() in order
	 * to be used again
	 */
	public void destroy() {
		if (!created)
			return;

		sendPacket(player, createObjectivePacket(1, null));
		for (VirtualTeam team : lines)
			if (team != null)
				sendPacket(player, team.removeTeam());

		created = false;
	}

	/**
	 * Change the name of the objective. The name is displayed at the top of the scoreboard.
	 * @param name the name of the objective, max 32 char
	 * @throws ClassNotFoundException reflection problem
	 */
	public void setObjectiveName(String name) throws ClassNotFoundException {
		this.objectiveName = name;
		if (created)
			sendPacket(player, createObjectivePacket(2, name));
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

			if (old != null && created)
				sendPacket(player, removeLine(old));

			while (containsValue(value, team)) {
				value = value + " "; //add a space if a line with the value already exists
			}
			team.setValue(value);
			sendLine(line);
			return team;
		}catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
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
			sendPacket(player, removeLine(old));
			sendPacket(player, team.removeTeam());
		}

		lines.remove(line);
		for (int i = line; i < lines.size(); i++) {
			VirtualTeam val = getOrCreateTeam(i);
			sendPacket(player, sendScore(val.getCurrentPlayer(), 15 - /*line ?*/ i));
		}
	}

	public void moveLines(int start, int amount) {
		int newSize = lines.size() + amount;
		for (int i = start; i < newSize; i++) { // from the start line to the end of the final list
			if (i < start + amount) { // insert null values to make space
				lines.add(start, null);
			}else { // refresh scores of the next lines
				VirtualTeam val = getOrCreateTeam(i);
				sendPacket(player, sendScore(val.getCurrentPlayer(), 15 - i));
			}
		}
	}

	/**
	 * Get the current value for a line
	 * @param line the line
	 * @return the content of the line
	 */
	public String getLine(int line) {
		if (line > 14)
			return null;
		if (line < 0)
			return null;
		return getOrCreateTeam(line).getValue();
	}

	/**
	 * Check if a line has the exact same value
	 * @param value line value to check
	 * @return true if a line has the same value
	 */
	public boolean containsValue(String value, VirtualTeam except) {
		for (VirtualTeam team : lines) {
			//if (team != null) System.out.println("VALUE: " + value + " | TEAM: " + team.getValue() + " | SAME: " + (except == team));
			if (team != null && team != except && value.equals(team.getValue())) return true;
		}
		return false;
	}

	/**
	 * Get the team assigned to a line
	 * @param line line number
	 * @return the VirtualTeam used to display this line
	 */
	public VirtualTeam getTeam(int line) {
		if (line > 14)
			return null;
		if (line < 0)
			return null;
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

	private void sendLine(int line) throws ClassNotFoundException {
		if (line > 14)
			return;
		if (line < 0)
			return;
		if (!created)
			return;

		int score = (15 - line);
		VirtualTeam val = getOrCreateTeam(line);
		for (Object packet : val.sendLine()) {
			sendPacket(player, packet);
		}
		sendPacket(player, sendScore(val.getCurrentPlayer(), score));
		val.reset();
	}

	private int last = 0;

	private VirtualTeam getOrCreateTeam(int line) {
		if (lines.size() <= line) {
			lines.add(new VirtualTeam("__fakeScore" + last));
			last++;
		}else if (lines.get(line) == null) {
			lines.set(line, new VirtualTeam("__fakeScore" + last));
			last++;
		}

		return lines.get(line);
	}

	/*
		Factories
		 */
	private Object createObjectivePacket(int mode, String displayName) {
		Object packet = new PacketPlayOutScoreboardObjective();
		// Nom de l'objectif
		setField(packet, "a", player.getName());

		// Mode
		// 0 : créer
		// 1 : Supprimer
		// 2 : Mettre à jour
		setField(packet, "d", mode);

		if (mode == 0 || mode == 2) {
			setField(packet, "b", new ChatComponentText(displayName));
			setField(packet, "c", EnumScoreboardHealthDisplay.INTEGER);
		}

		return packet;
	}

	private Object setObjectiveSlot() {
		Object packet = new PacketPlayOutScoreboardDisplayObjective();
		// Slot
		setField(packet, "a", 1);
		setField(packet, "b", player.getName());

		return packet;
	}

	private Object sendScore(String line, int score) {
		Object packet = new PacketPlayOutScoreboardScore();
		setField(packet, "a", line);
		setField(packet, "b", player.getName());
		setField(packet, "c", score);
		setField(packet, "d", Action.CHANGE);
		return packet;
	}

	private Object removeLine(String line) {
		Object packet = new PacketPlayOutScoreboardScore();
		setField(packet, "a", line);
		setField(packet, "d", Action.REMOVE);
		return packet;
	}

	/**
	 * This class is used to manage the content of a line. Advanced users can use it as they want, but they are encouraged to read and understand the
	 * code before doing so. Use these methods at your own risk.
	 */
	public class VirtualTeam {
		private final String name;
		private String prefix;
		private String suffix;
		private String currentPlayer;
		private String oldPlayer;
		private String cachedValue;

		private boolean prefixChanged, suffixChanged, playerChanged = false;
		private boolean first = true;

		private VirtualTeam(String name, String prefix, String suffix) {
			this.name = name;
			this.prefix = prefix;
			this.suffix = suffix;
			this.cachedValue = "";
		}

		private VirtualTeam(String name) {
			this(name, "", "");
		}

		public String getName() {
			return name;
		}

		public String getPrefix() {
			return prefix;
		}

		private void setPrefix(String prefix) {
			if (this.prefix == null || !this.prefix.equals(prefix))
				this.prefixChanged = true;
			this.prefix = prefix;
		}

		public String getSuffix() {
			return suffix;
		}

		private void setSuffix(String suffix) {
			if (this.suffix == null || !this.suffix.equals(prefix))
				this.suffixChanged = true;
			this.suffix = suffix;
		}

		private Object createPacket(int mode) {
			Object packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", name);
			setField(packet, "i", mode);
			setField(packet, "b", new ChatComponentText(""));
			setField(packet, "c", new ChatComponentText(prefix));
			setField(packet, "d", new ChatComponentText(suffix));
			setField(packet, "j", 0);
			setField(packet, "e", "always");

			return packet;
		}

		public Object createTeam() {
			return createPacket(0);
		}

		public Object updateTeam() {
			return createPacket(2);
		}

		public Object removeTeam() {
			Object packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", name);
			setField(packet, "i", 1);
			first = true;
			return packet;
		}

		private void setPlayer(String name) {
			if (this.currentPlayer == null || !this.currentPlayer.equals(name))
				this.playerChanged = true;
			this.oldPlayer = this.currentPlayer;
			this.currentPlayer = name;
		}

		public Iterable<Object> sendLine() {
			List<Object> packets = new ArrayList<>();

			if (first) {
				packets.add(createTeam());
			}else if (prefixChanged || suffixChanged) {
				packets.add(updateTeam());
			}

			if (first || playerChanged) {
				if (oldPlayer != null) // remove these two lines ?
					packets.add(addOrRemovePlayer(4, oldPlayer)); //
				packets.add(changePlayer());
			}

			if (first)
				first = false;

			return packets;
		}

		public void reset() {
			prefixChanged = false;
			suffixChanged = false;
			playerChanged = false;
			oldPlayer = null;
		}

		public Object changePlayer() {
			return addOrRemovePlayer(3, currentPlayer);
		}

		public Object addOrRemovePlayer(int mode, String playerName) {
			Object packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", name);
			setField(packet, "i", mode);

			try {
				Field f = packet.getClass().getDeclaredField("h");
				f.setAccessible(true);
				((List<String>) f.get(packet)).add(playerName);
			}catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}

			return packet;
		}

		public String getCurrentPlayer() {
			return currentPlayer;
		}

		public String getValue() {
			return cachedValue;
		}

		public void setValue(String value) {
			if (value.length() <= 16) {
				setPrefix("");
				setSuffix("");
				setPlayer(value);
			}else if (value.length() <= 32) {
				setPrefix(value.substring(0, 16));
				setPlayer(value.substring(16));
				setSuffix("");
			}else if (value.length() <= 48) {
				setPrefix(value.substring(0, 16));
				setPlayer(value.substring(16, 32));
				setSuffix(value.substring(32));
			}else {
				throw new IllegalArgumentException("Too long value ! Max 48 characters, value was " + value.length() + " !");
			}
			this.cachedValue = value;
		}
	}

	private static void sendPacket(Player p, Object packet) {
		((CraftPlayer) p).getHandle().playerConnection.sendPacket((Packet<?>) packet);
	}

	private static void setField(Object edit, String fieldName, Object value) {
		Validate.notNull(edit);
		try {
			Field field = edit.getClass().getDeclaredField(fieldName);
			field.setAccessible(true);
			field.set(edit, value);
		}catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}