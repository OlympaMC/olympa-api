package fr.olympa.api.scoreboard;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import fr.olympa.api.utils.Reflection;
import fr.olympa.api.utils.Reflection.ClassEnum;

/**
 * Simple Bukkit ScoreBoard API with 1.7 to 1.15 support !
 * Everything is at packet level so you don't need to use it in the main server thread.
 * <p>
 * You can find the project on <a href="https://github.com/MrMicky-FR/FastBoard">GitHub</a>
 *
 * @author MrMicky
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class FastBoardAPIold {

	enum ObjectiveMode {
		CREATE,
		REMOVE,
		UPDATE
	}

	// Packets sending
	enum ScoreboardAction {
		CHANGE,
		REMOVE
	}

	enum TeamMode {
		CREATE,
		REMOVE,
		UPDATE,
		ADD_PLAYERS,
		REMOVE_PLAYERS
	}

	enum VersionType {
		V1_7,
		V1_8,
		V1_13;
		public boolean isHigherOrEqual() {
			return VERSION_TYPE.ordinal() >= this.ordinal();
		}
	}

	// Chat components
	private static final VersionType VERSION_TYPE;

	// Scoreboard enums
	private static final Class<?> ENUM_SB_HEALTH_DISPLAY;

	private static final Class<?> ENUM_SB_ACTION;

	private static final Object ENUM_SB_HEALTH_DISPLAY_INTEGER;
	private static final Object ENUM_SB_ACTION_CHANGE;

	private static final Object ENUM_SB_ACTION_REMOVE;
	static {
		if (Reflection.getClass(ClassEnum.NMS, "ScoreboardServer$Action") != null) {
			VERSION_TYPE = VersionType.V1_13;
		} else if (Reflection.getClass(ClassEnum.NMS, "IScoreboardCriteria$EnumScoreboardHealthDisplay") != null) {
			VERSION_TYPE = VersionType.V1_8;
		} else {
			VERSION_TYPE = VersionType.V1_7;
		}

		if (VersionType.V1_8.isHigherOrEqual()) {
			ENUM_SB_HEALTH_DISPLAY = Reflection.getClass(ClassEnum.NMS, "IScoreboardCriteria$EnumScoreboardHealthDisplay");
			if (VersionType.V1_13.isHigherOrEqual()) {
				ENUM_SB_ACTION = Reflection.getClass(ClassEnum.NMS, "ScoreboardServer$Action");
			} else {
				ENUM_SB_ACTION = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutScoreboardScore$EnumScoreboardAction");
			}

			ENUM_SB_HEALTH_DISPLAY_INTEGER = Enum.valueOf((Class<Enum>) ENUM_SB_HEALTH_DISPLAY, "INTEGER");
			ENUM_SB_ACTION_CHANGE = Enum.valueOf((Class<Enum>) ENUM_SB_ACTION, "CHANGE");
			ENUM_SB_ACTION_REMOVE = Enum.valueOf((Class<Enum>) ENUM_SB_ACTION, "REMOVE");
		} else {
			ENUM_SB_HEALTH_DISPLAY = null;
			ENUM_SB_ACTION = null;

			ENUM_SB_HEALTH_DISPLAY_INTEGER = null;
			ENUM_SB_ACTION_CHANGE = null;
			ENUM_SB_ACTION_REMOVE = null;
		}
	}

	private final Player player;

	private final String id;

	private String title = ChatColor.RESET.toString();

	private List<String> lines = new ArrayList<>();

	private boolean deleted = false;

	/**
	 * Create a new FastBoard for a player
	 *
	 * @param player the player the scoreboard is for
	 */
	public FastBoardAPIold(Player player) {
		this.player = Objects.requireNonNull(player, "player");

		this.id = "fb-" + Double.toString(Math.random()).substring(2, 10);

		try {
			this.sendObjectivePacket(ObjectiveMode.CREATE);
			this.sendDisplayObjectivePacket();
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Delete this FastBoard, and will remove the scoreboard for the associated player if he is online.
	 * After this, all uses of {@link #updateLines} and {@link #updateTitle} will throws an {@link IllegalStateException}
	 *
	 * @throws IllegalStateException if this was already call before
	 */
	public void delete() {
		try {
			for (int i = 0; i < this.lines.size(); i++) {
				this.sendTeamPacket(i, TeamMode.REMOVE);
			}

			this.sendObjectivePacket(ObjectiveMode.REMOVE);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}

		this.deleted = true;
	}

	private String getColorCode(int score) {
		return ChatColor.values()[score].toString();
	}

	/**
	 * Get the id of theFastBoard
	 *
	 * @return id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Get the specified scoreboard line
	 *
	 * @param line the line number
	 * @return the line
	 * @throws IndexOutOfBoundsException if the number is higher than the number of lines
	 */
	public String getLine(int line) {
		return this.lines.get(line);
	}

	private String getLineByScore(int score) {
		return this.getLineByScore(this.lines, score);
	}

	private String getLineByScore(List<String> lines, int score) {
		return lines.get(lines.size() - score - 1);
	}

	/**
	 * Get the current lines of the scoreboard
	 *
	 * @return the current lines of the scoreboard
	 */
	public List<String> getLines() {
		return new ArrayList<>(this.lines);
	}

	/**
	 * Get the player associated with this FastBoard
	 *
	 * @return current player for this FastBoard
	 */
	public Player getPlayer() {
		return this.player;
	}

	/**
	 * Get the current title of the scoreboard.
	 *
	 * @return current scoreboard title
	 */
	public String getTitle() {
		return this.title;
	}

	public boolean isDeleted() {
		return this.deleted;
	}

	private void sendDisplayObjectivePacket() throws ReflectiveOperationException {
		Object packet = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutScoreboardDisplayObjective").getConstructor().newInstance();

		this.setField(packet, int.class, 1);
		this.setField(packet, String.class, this.id);

		this.sendPacket(packet);
	}

	private void sendObjectivePacket(ObjectiveMode mode) throws ReflectiveOperationException {
		Object packet = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutScoreboardObjective").getConstructor().newInstance();

		this.setField(packet, String.class, this.id);
		this.setField(packet, int.class, mode.ordinal());

		if (mode != ObjectiveMode.REMOVE) {
			this.setComponentField(packet, this.title, 1);

			if (VersionType.V1_8.isHigherOrEqual()) {
				this.setField(packet, ENUM_SB_HEALTH_DISPLAY, ENUM_SB_HEALTH_DISPLAY_INTEGER);
			}
		} else if (VERSION_TYPE == VersionType.V1_7) {
			this.setField(packet, String.class, "", 1);
		}

		this.sendPacket(packet);
	}

	private void sendPacket(Object packet) throws ReflectiveOperationException {
		if (this.deleted) {
			throw new IllegalStateException("This FastBoard is deleted");
		}

		if (this.player.isOnline()) {
			Reflection.sendPacket(this.player, packet);
		}
	}

	private void sendScorePacket(int score, ScoreboardAction action) throws ReflectiveOperationException {
		Object packet = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutScoreboardScore").getConstructor().newInstance();

		this.setField(packet, String.class, this.getColorCode(score), 0);

		if (VersionType.V1_8.isHigherOrEqual()) {
			this.setField(packet, ENUM_SB_ACTION, action == ScoreboardAction.REMOVE ? ENUM_SB_ACTION_REMOVE : ENUM_SB_ACTION_CHANGE);
		} else {
			this.setField(packet, int.class, action.ordinal(), 1);
		}

		if (action == ScoreboardAction.CHANGE) {
			this.setField(packet, String.class, this.id, 1);
			this.setField(packet, int.class, score);
		}

		this.sendPacket(packet);
	}

	private void sendTeamPacket(int score, TeamMode mode) throws ReflectiveOperationException {
		if (mode == TeamMode.ADD_PLAYERS || mode == TeamMode.REMOVE_PLAYERS) {
			throw new UnsupportedOperationException();
		}

		Object packet = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutScoreboardTeam").getConstructor().newInstance();

		this.setField(packet, String.class, this.id + ':' + score); // Team name
		this.setField(packet, int.class, mode.ordinal(), VERSION_TYPE == VersionType.V1_8 ? 1 : 0); // Update mode

		if (mode == TeamMode.CREATE || mode == TeamMode.UPDATE) {
			String line = this.getLineByScore(score);
			String prefix;
			String suffix = null;

			if (line == null || line.isEmpty()) {
				prefix = this.getColorCode(score) + ChatColor.RESET;
			} else if (line.length() <= 16 || VersionType.V1_13.isHigherOrEqual()) {
				prefix = line;
			} else {
				// Prevent splitting color codes
				int index = line.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16;
				prefix = line.substring(0, index);
				String suffixTmp = line.substring(index);
				ChatColor chatColor = null;

				if (suffixTmp.length() >= 2 && suffixTmp.charAt(0) == ChatColor.COLOR_CHAR) {
					chatColor = ChatColor.getByChar(suffixTmp.charAt(1));
				}

				String color = ChatColor.getLastColors(prefix);
				boolean addColor = chatColor == null || chatColor.isFormat();

				suffix = (addColor ? color.isEmpty() ? ChatColor.RESET : color : "") + suffixTmp;
			}

			if (VERSION_TYPE != VersionType.V1_13) {
				// Something went wrong, just cut to prevent client crash/kick
				if (prefix.length() > 16) {
					prefix = prefix.substring(0, 16);
				}
				if (suffix != null && suffix.length() > 16) {
					suffix = suffix != null ? suffix.substring(0, 16) : null;
				}
			}

			this.setComponentField(packet, prefix, 2); // Prefix
			this.setComponentField(packet, suffix == null ? "" : suffix, 3); // Suffix
			this.setField(packet, String.class, "always", 4); // Visibility for 1.8+
			this.setField(packet, String.class, "always", 5); // Collisions for 1.9+

			if (mode == TeamMode.CREATE) {
				this.setField(packet, Collection.class, Collections.singletonList(this.getColorCode(score))); // Players in the team
			}
		}

		this.sendPacket(packet);
	}

	private void setComponentField(Object object, String value, int count) throws ReflectiveOperationException {
		if (VERSION_TYPE != VersionType.V1_13) {
			this.setField(object, String.class, value, count);
			return;
		}

		int i = 0;
		for (Field f : object.getClass().getDeclaredFields()) {
			if ((f.getType() == String.class || f.getType() == Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent")) && i++ == count) {
				f.setAccessible(true);
				f.set(object, Array.get(Reflection.getClass(ClassEnum.CB, "util.CraftChatMessage").getDeclaredMethod("fromString", String.class).invoke(null, value), 0));
			}
		}
	}

	private void setField(Object object, Class<?> fieldType, Object value) throws ReflectiveOperationException {
		this.setField(object, fieldType, value, 0);
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

	/**
	 * Update the lines of the scoreboard
	 *
	 * @param lines the new scoreboard lines
	 * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
	 * @throws IllegalStateException    if {@link #delete()} was call before
	 */
	public void updateLines(Collection<String> lines) {
		Objects.requireNonNull(lines, "lines");

		if (!VersionType.V1_13.isHigherOrEqual()) {
			int lineCount = 0;
			for (String s : lines) {
				if (s != null && s.length() > 30) {
					throw new IllegalArgumentException("Line " + lineCount + " is longer than 30 chars");
				}
				lineCount++;
			}
		}

		List<String> oldLines = new ArrayList<>(this.lines);
		this.lines.clear();
		this.lines.addAll(lines);

		int linesSize = this.lines.size();

		try {
			if (oldLines.size() != linesSize) {
				List<String> oldLinesCopy = new ArrayList<>(oldLines);

				if (oldLines.size() > linesSize) {
					for (int i = oldLinesCopy.size(); i > linesSize; i--) {
						this.sendTeamPacket(i - 1, TeamMode.REMOVE);

						this.sendScorePacket(i - 1, ScoreboardAction.REMOVE);

						oldLines.remove(0);
					}
				} else {
					for (int i = oldLinesCopy.size(); i < linesSize; i++) {
						this.sendScorePacket(i, ScoreboardAction.CHANGE);

						this.sendTeamPacket(i, TeamMode.CREATE);

						oldLines.add(oldLines.size() - i, this.getLineByScore(i));
					}
				}
			}

			for (int i = 0; i < linesSize; i++) {
				if (!Objects.equals(this.getLineByScore(oldLines, i), this.getLineByScore(i))) {
					this.sendTeamPacket(i, TeamMode.UPDATE);
				}
			}
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Update the lines of the scoreboard
	 *
	 * @param lines the new scoreboard lines
	 * @throws IllegalArgumentException if one line is longer than 30 chars on 1.12 or lower
	 * @throws IllegalStateException    if {@link #delete()} was call before
	 */
	public void updateLines(String... lines) {
		this.updateLines(Arrays.asList(lines));
	}

	/**
	 * Update the scoreboard title. The title can't be longer than 32 chars
	 *
	 * @param title the new scoreboard title
	 * @throws IllegalArgumentException if the title is longer than 32 chars on 1.12 or lower
	 * @throws IllegalStateException    if {@link #delete()} was call before
	 */
	public void updateTitle(String title) {
		if (this.title.equals(Objects.requireNonNull(title, "title"))) {
			return;
		}

		if (!VersionType.V1_13.isHigherOrEqual() && title.length() > 32) {
			throw new IllegalArgumentException("Title is longer than 32 chars");
		}

		this.title = title;

		try {
			this.sendObjectivePacket(ObjectiveMode.UPDATE);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
