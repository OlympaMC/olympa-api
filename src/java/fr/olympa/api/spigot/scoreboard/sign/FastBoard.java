package fr.olympa.api.spigot.scoreboard.sign;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

import fr.olympa.api.spigot.utils.ProtocolAPI;
import fr.olympa.core.spigot.OlympaCore;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.IScoreboardCriteria;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_16_R3.ScoreboardServer;
import net.minecraft.server.v1_16_R3.ScoreboardServer.Action;

/**
 * Simple Bukkit ScoreBoard API with 1.7 to 1.16 support.
 * Everything is at packet level so you don't need to use it in the main server thread.
 * <p>
 * You can find the project on <a href="https://github.com/MrMicky-FR/FastBoard">GitHub</a>
 *
 * @author MrMicky
 */
public class FastBoard {
	
	private final Player player;
	private final String id;
	
	private String title = ChatColor.RESET.toString();
	private List<String> lines = new ArrayList<>();
	
	private boolean deleted = false;
	
	private final boolean below13;
	
	/**
	 * Creates a new FastBoard.
	 *
	 * @param player the player the scoreboard is for
	 */
	public FastBoard(Player player) {
		this.player = Objects.requireNonNull(player, "player");
		
		id = "fb-" + Double.toString(Math.random()).substring(2, 10);
		
		below13 = (OlympaCore.getInstance().getProtocolSupport() == null ? ProtocolAPI.getDefaultSpigotProtocol() : OlympaCore.getInstance().getProtocolSupport().getPlayerVersion(player)).ordinal() > ProtocolAPI.V1_13.ordinal();
		
		try {
			sendObjectivePacket(ObjectiveMode.CREATE);
			sendDisplayObjectivePacket();
		}catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get the scoreboard title.
	 *
	 * @return the scoreboard title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Update the scoreboard title.
	 *
	 * @param title the new scoreboard title
	 * @throws IllegalArgumentException if the title is longer than 32 chars on 1.12 or lower
	 * @throws IllegalStateException    if {@link #delete()} was call before
	 */
	public void updateTitle(String title) {
		if (this.title.equals(Objects.requireNonNull(title, "title"))) {
			return;
		}
		
		this.title = title;
		
		try {
			sendObjectivePacket(ObjectiveMode.UPDATE);
		}catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get the scoreboard lines.
	 *
	 * @return the scoreboard lines
	 */
	public List<String> getLines() {
		return new ArrayList<>(lines);
	}
	
	/**
	 * Get the specified scoreboard line.
	 *
	 * @param line the line number
	 * @return the line
	 * @throws IndexOutOfBoundsException if the line is higher than {@code size}
	 */
	public String getLine(int line) {
		checkLineNumber(line, true);
		
		return lines.get(line);
	}
	
	/**
	 * Update a single scoreboard line.
	 *
	 * @param line the line number
	 * @param text the new line text
	 * @throws IndexOutOfBoundsException if the line is higher than {@code size} + 1
	 */
	public void updateLine(int line, String text) {
		checkLineNumber(line, false);
		
		try {
			if (line < size()) {
				lines.set(line, text);
				
				sendTeamPacket(getScoreByLine(line), TeamMode.UPDATE);
				return;
			}
			
			List<String> newLines = new ArrayList<>(lines);
			
			if (line > size()) {
				for (int i = size(); i < line; i++) {
					newLines.add("");
				}
			}
			
			newLines.add(text);
			
			updateLines(newLines);
		}catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Remove a scoreboard line.
	 *
	 * @param line the line number
	 */
	public void removeLine(int line) {
		checkLineNumber(line, false);
		
		if (line >= size()) {
			return; // The line don't exists
		}
		
		List<String> lines = new ArrayList<>(this.lines);
		lines.remove(line);
		updateLines(lines);
	}
	
	/**
	 * Update all the scoreboard lines.
	 *
	 * @param lines the new lines
	 * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
	 * @throws IllegalStateException    if {@link #delete()} was call before
	 */
	public void updateLines(String... lines) {
		updateLines(Arrays.asList(lines));
	}
	
	/**
	 * Update the lines of the scoreboard
	 *
	 * @param lines the new scoreboard lines
	 * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
	 * @throws IllegalStateException    if {@link #delete()} was call before
	 */
	public void updateLines(Collection<String> lines) {
		Objects.requireNonNull(lines, "lines");
		
		List<String> oldLines = new ArrayList<>(this.lines);
		this.lines.clear();
		this.lines.addAll(lines);
		
		int linesSize = this.lines.size();
		
		try {
			if (oldLines.size() != linesSize) {
				List<String> oldLinesCopy = new ArrayList<>(oldLines);
				
				if (oldLines.size() > linesSize) {
					for (int i = oldLinesCopy.size(); i > linesSize; i--) {
						sendTeamPacket(i - 1, TeamMode.REMOVE);
						
						sendScorePacket(i - 1, Action.REMOVE);
						
						oldLines.remove(0);
					}
				}else {
					for (int i = oldLinesCopy.size(); i < linesSize; i++) {
						sendScorePacket(i, Action.CHANGE);
						
						sendTeamPacket(i, TeamMode.CREATE);
						
						oldLines.add(oldLines.size() - i, getLineByScore(i));
					}
				}
			}
			
			for (int i = 0; i < linesSize; i++) {
				if (!Objects.equals(getLineByScore(oldLines, i), getLineByScore(i))) {
					sendTeamPacket(i, TeamMode.UPDATE);
				}
			}
		}catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Get the player who has the scoreboard.
	 *
	 * @return current player for this FastBoard
	 */
	public Player getPlayer() {
		return player;
	}
	
	/**
	 * Get the scoreboard id.
	 *
	 * @return the id
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Get if the scoreboard is deleted.
	 *
	 * @return true if the scoreboard is deleted
	 */
	public boolean isDeleted() {
		return deleted;
	}
	
	/**
	 * Get the scoreboard size (the number of lines).
	 *
	 * @return the size
	 */
	public int size() {
		return lines.size();
	}
	
	/**
	 * Delete this FastBoard, and will remove the scoreboard for the associated player if he is online.
	 * After this, all uses of {@link #updateLines} and {@link #updateTitle} will throws an {@link IllegalStateException}
	 *
	 * @throws IllegalStateException if this was already call before
	 */
	public void delete() {
		try {
			for (int i = 0; i < lines.size(); i++) {
				sendTeamPacket(i, TeamMode.REMOVE);
			}
			
			sendObjectivePacket(ObjectiveMode.REMOVE);
		}catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
		
		deleted = true;
	}
	
	private void checkLineNumber(int line, boolean checkMax) {
		if (line < 0) {
			throw new IllegalArgumentException("Line number must be positive");
		}
		
		if (checkMax && line >= lines.size()) {
			throw new IllegalArgumentException("Line number must be under " + lines.size());
		}
	}
	
	private int getScoreByLine(int line) {
		return lines.size() - line - 1;
	}
	
	private String getLineByScore(int score) {
		return getLineByScore(lines, score);
	}
	
	private String getLineByScore(List<String> lines, int score) {
		return lines.get(lines.size() - score - 1);
	}
	
	private void sendObjectivePacket(ObjectiveMode mode) throws ReflectiveOperationException {
		PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective();
		
		setField(packet, String.class, id);
		setField(packet, int.class, mode.ordinal());
		
		if (mode != ObjectiveMode.REMOVE) {
			setComponentField(packet, title, 1);
			
			setField(packet, IScoreboardCriteria.EnumScoreboardHealthDisplay.class, IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER);
		}
		
		sendPacket(packet);
	}
	
	private void sendDisplayObjectivePacket() throws ReflectiveOperationException {
		PacketPlayOutScoreboardDisplayObjective packet = new PacketPlayOutScoreboardDisplayObjective();
		
		setField(packet, int.class, 1);
		setField(packet, String.class, id);
		
		sendPacket(packet);
	}
	
	private void sendScorePacket(int score, ScoreboardServer.Action action) throws ReflectiveOperationException {
		PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(action, id, getColorCode(score), score);
		
		sendPacket(packet);
	}
	
	private void sendTeamPacket(int score, TeamMode mode) throws ReflectiveOperationException {
		if (mode == TeamMode.ADD_PLAYERS || mode == TeamMode.REMOVE_PLAYERS) {
			throw new UnsupportedOperationException();
		}
		
		PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();
		
		setField(packet, String.class, id + ':' + score); // Team name
		setField(packet, int.class, mode.ordinal(), 0); // Update mode
		
		if (mode == TeamMode.CREATE || mode == TeamMode.UPDATE) {
			String line = getLineByScore(score);
			String prefix;
			String suffix = null;
			
			if (line == null || line.isEmpty()) {
				prefix = getColorCode(score) + ChatColor.RESET;
			}else if (!below13 || line.length() < 12) {
				prefix = line;
			}else {
				// Prevent splitting color codes
				int index = line.charAt(11) == ChatColor.COLOR_CHAR ? 11 : 12;
				prefix = line.substring(0, index);
				String suffixTmp = line.substring(index);
				ChatColor chatColor = null;
				
				if (suffixTmp.length() >= 2 && suffixTmp.charAt(0) == ChatColor.COLOR_CHAR) {
					chatColor = ChatColor.getByChar(suffixTmp.charAt(1));
				}
				
				String color = ChatColor.getLastColors(prefix);
				boolean addColor = chatColor == null || chatColor.isFormat();
				
				suffix = (addColor ? (color.isEmpty() ? ChatColor.RESET.toString() : color) : "") + suffixTmp;
			}
			
			/*if (below13) {
				if (prefix.length() > 16 || (suffix != null && suffix.length() > 16)) {
					// Something went wrong, just cut to prevent client crash/kick
					prefix = prefix.substring(0, 16);
					suffix = (suffix != null) ? suffix.substring(0, 16) : null;
				}
			}*/
			
			setComponentField(packet, prefix, 2); // Prefix
			setComponentField(packet, suffix == null ? "" : suffix, 3); // Suffix
			setField(packet, String.class, "always", 4); // Visibility for 1.8+
			setField(packet, String.class, "always", 5); // Collisions for 1.9+
			
			if (mode == TeamMode.CREATE) {
				setField(packet, Collection.class, Collections.singletonList(getColorCode(score))); // Players in the team
			}
		}
		
		sendPacket(packet);
	}
	
	private String getColorCode(int score) {
		return ChatColor.values()[score].toString();
	}
	
	private void sendPacket(Packet<?> packet) throws ReflectiveOperationException {
		if (deleted) {
			throw new IllegalStateException("This FastBoard is deleted");
		}
		
		if (player.isOnline()) {
			((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
		}
	}
	
	private void setField(Object object, Class<?> fieldType, Object value) throws ReflectiveOperationException {
		setField(object, fieldType, value, 0);
	}
	
	private void setField(Object object, Class<?> fieldType, Object value, int count) throws ReflectiveOperationException {
		int i = 0;
		
		for (Field f : object.getClass().getDeclaredFields()) {
			if (f.getType() == fieldType && i++ == count) {
				f.setAccessible(true);
				f.set(object, value);
			}
		}
	}
	
	private void setComponentField(Object object, String value, int count) throws ReflectiveOperationException {
		int i = 0;
		for (Field f : object.getClass().getDeclaredFields()) {
			if ((f.getType() == String.class || f.getType() == IChatBaseComponent.class) && i++ == count) {
				f.setAccessible(true);
				f.set(object, Array.get(CraftChatMessage.fromString(value), 0));
			}
		}
	}
	
	enum ObjectiveMode {
		
		CREATE, REMOVE, UPDATE
	
	}
	
	enum TeamMode {
		
		CREATE, REMOVE, UPDATE, ADD_PLAYERS, REMOVE_PLAYERS
	
	}
}