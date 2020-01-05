package fr.olympa.api.scoreboard;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.IScoreboardCriteria.EnumScoreboardHealthDisplay;
import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_15_R1.ScoreboardServer.Action;

/**
 * A simple tool to manage scoreboards in minecraft (lines up to 48 characters !).<br>
 * Edited by me to permit more flexibility
 * @see <a href="https://gist.github.com/zyuiop/8fcf2ca47794b92d7caa">Original file on GitHub</a>
 * @author zyuiop, SkytAsul
 */
public class ScoreboardSigns {

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

		private VirtualTeam(String name) {
			this(name, "", "");
		}

		private VirtualTeam(String name, String prefix, String suffix) {
			this.name = name;
			this.prefix = prefix;
			this.suffix = suffix;
			this.cachedValue = "";
		}

		public Object addOrRemovePlayer(int mode, String playerName) {
			Object packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", this.name);
			setField(packet, "i", mode);

			try {
				Field f = packet.getClass().getDeclaredField("h");
				f.setAccessible(true);
				((List<String>) f.get(packet)).add(playerName);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				e.printStackTrace();
			}

			return packet;
		}

		public Object changePlayer() {
			return this.addOrRemovePlayer(3, this.currentPlayer);
		}

		private Object createPacket(int mode) {
			Object packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", this.name);
			setField(packet, "i", mode);
			setField(packet, "b", new ChatComponentText(""));
			setField(packet, "c", new ChatComponentText(this.prefix));
			setField(packet, "d", new ChatComponentText(this.suffix));
			setField(packet, "j", 0);
			setField(packet, "e", "always");

			return packet;
		}

		public Object createTeam() {
			return this.createPacket(0);
		}

		public String getCurrentPlayer() {
			return this.currentPlayer;
		}

		public String getName() {
			return this.name;
		}

		public String getPrefix() {
			return this.prefix;
		}

		public String getSuffix() {
			return this.suffix;
		}

		public String getValue() {
			return this.cachedValue;
		}

		public Object removeTeam() {
			Object packet = new PacketPlayOutScoreboardTeam();
			setField(packet, "a", this.name);
			setField(packet, "i", 1);
			this.first = true;
			return packet;
		}

		public void reset() {
			this.prefixChanged = false;
			this.suffixChanged = false;
			this.playerChanged = false;
			this.oldPlayer = null;
		}

		public Iterable<Object> sendLine() {
			List<Object> packets = new ArrayList<>();

			if (this.first) {
				packets.add(this.createTeam());
			} else if (this.prefixChanged || this.suffixChanged) {
				packets.add(this.updateTeam());
			}

			if (this.first || this.playerChanged) {
				if (this.oldPlayer != null) {
					packets.add(this.addOrRemovePlayer(4, this.oldPlayer)); //
				}
				packets.add(this.changePlayer());
			}

			if (this.first) {
				this.first = false;
			}

			return packets;
		}

		private void setPlayer(String name) {
			if (this.currentPlayer == null || !this.currentPlayer.equals(name)) {
				this.playerChanged = true;
			}
			this.oldPlayer = this.currentPlayer;
			this.currentPlayer = name;
		}

		private void setPrefix(String prefix) {
			if (this.prefix == null || !this.prefix.equals(prefix)) {
				this.prefixChanged = true;
			}
			this.prefix = prefix;
		}

		private void setSuffix(String suffix) {
			if (this.suffix == null || !this.suffix.equals(this.prefix)) {
				this.suffixChanged = true;
			}
			this.suffix = suffix;
		}

		public void setValue(String value) {
			if (value.length() <= 16) {
				this.setPrefix("");
				this.setSuffix("");
				this.setPlayer(value);
			} else if (value.length() <= 32) {
				this.setPrefix(value.substring(0, 16));
				this.setPlayer(value.substring(16));
				this.setSuffix("");
			} else if (value.length() <= 48) {
				this.setPrefix(value.substring(0, 16));
				this.setPlayer(value.substring(16, 32));
				this.setSuffix(value.substring(32));
			} else {
				throw new IllegalArgumentException("Too long value ! Max 48 characters, value was " + value.length() + " !");
			}
			this.cachedValue = value;
		}

		public Object updateTeam() {
			return this.createPacket(2);
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
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	private boolean created = false;

	private final ArrayList<VirtualTeam> lines = new ArrayList<>(15);

	private final Player player;

	private String objectiveName;

	private int last = 0;

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
	 * Check if a line has the exact same value
	 * @param value line value to check
	 * @return true if a line has the same value
	 */
	public boolean containsValue(String value, VirtualTeam except) {
		for (VirtualTeam team : this.lines) {
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
		if (this.created) {
			return;
		}

		try {
			sendPacket(this.player, this.createObjectivePacket(0, this.objectiveName));
			sendPacket(this.player, this.setObjectiveSlot());
			int i = 0;
			while (i < this.lines.size()) {
				this.sendLine(i++);
			}

			this.created = true;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Factories
	 */
	private Object createObjectivePacket(int mode, String displayName) {
		Object packet = new PacketPlayOutScoreboardObjective();
		// Nom de l'objectif
		setField(packet, "a", this.player.getName());

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

	/**
	 * Send the packets to remove this scoreboard sign. A destroyed scoreboard sign must be recreated using create() in order
	 * to be used again
	 */
	public void destroy() {
		if (!this.created) {
			return;
		}

		sendPacket(this.player, this.createObjectivePacket(1, null));
		for (VirtualTeam team : this.lines) {
			if (team != null) {
				sendPacket(this.player, team.removeTeam());
			}
		}

		this.created = false;
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
		return this.getOrCreateTeam(line).getValue();
	}

	private VirtualTeam getOrCreateTeam(int line) {
		if (this.lines.size() <= line) {
			this.lines.add(new VirtualTeam("__fakeScore" + this.last));
			this.last++;
		} else if (this.lines.get(line) == null) {
			this.lines.set(line, new VirtualTeam("__fakeScore" + this.last));
			this.last++;
		}

		return this.lines.get(line);
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
		return this.getOrCreateTeam(line);
	}

	/**
	 * Get the line assigned to a team
	 * @param team Team object to get index of
	 * @return the line number assigned to the specified team
	 */
	public int getTeamLine(VirtualTeam team) {
		return this.lines.indexOf(team);
	}

	public void moveLines(int start, int amount) {
		int newSize = this.lines.size() + amount;
		for (int i = start; i < newSize; i++) { // from the start line to the end of the final list
			if (i < start + amount) { // insert null values to make space
				this.lines.add(start, null);
			} else { // refresh scores of the next lines
				VirtualTeam val = this.getOrCreateTeam(i);
				sendPacket(this.player, this.sendScore(val.getCurrentPlayer(), 15 - i));
			}
		}
	}

	/**
	 * Remove a given scoreboard line
	 * @param line the line to remove
	 */
	public void removeLine(int line) {
		VirtualTeam team = this.getOrCreateTeam(line);
		String old = team.getCurrentPlayer();

		if (old != null && this.created) {
			sendPacket(this.player, this.removeLine(old));
			sendPacket(this.player, team.removeTeam());
		}

		this.lines.remove(line);
		for (int i = line; i < this.lines.size(); i++) {
			VirtualTeam val = this.getOrCreateTeam(i);
			sendPacket(this.player, this.sendScore(val.getCurrentPlayer(), 15 - /* line ? */ i));
		}
	}

	private Object removeLine(String line) {
		Object packet = new PacketPlayOutScoreboardScore();
		setField(packet, "a", line);
		setField(packet, "d", Action.REMOVE);
		return packet;
	}

	private void sendLine(int line) throws ClassNotFoundException {
		if (line > 14) {
			return;
		}
		if (line < 0) {
			return;
		}
		if (!this.created) {
			return;
		}

		int score = 15 - line;
		VirtualTeam val = this.getOrCreateTeam(line);
		for (Object packet : val.sendLine()) {
			sendPacket(this.player, packet);
		}
		sendPacket(this.player, this.sendScore(val.getCurrentPlayer(), score));
		val.reset();
	}

	private Object sendScore(String line, int score) {
		Object packet = new PacketPlayOutScoreboardScore();
		setField(packet, "a", line);
		setField(packet, "b", this.player.getName());
		setField(packet, "c", score);
		setField(packet, "d", Action.CHANGE);
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
			VirtualTeam team = this.getOrCreateTeam(line);
			String old = team.getCurrentPlayer();

			if (old != null && this.created) {
				sendPacket(this.player, this.removeLine(old));
			}

			while (this.containsValue(value, team)) {
				value = value + " "; // add a space if a line with the value already exists
			}
			team.setValue(value);
			this.sendLine(line);
			return team;
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 * Change the name of the objective. The name is displayed at the top of the scoreboard.
	 * @param name the name of the objective, max 32 char
	 * @throws ClassNotFoundException reflection problem
	 */
	public void setObjectiveName(String name) throws ClassNotFoundException {
		this.objectiveName = name;
		if (this.created) {
			sendPacket(this.player, this.createObjectivePacket(2, name));
		}
	}

	private Object setObjectiveSlot() {
		Object packet = new PacketPlayOutScoreboardDisplayObjective();
		// Slot
		setField(packet, "a", 1);
		setField(packet, "b", this.player.getName());

		return packet;
	}
}