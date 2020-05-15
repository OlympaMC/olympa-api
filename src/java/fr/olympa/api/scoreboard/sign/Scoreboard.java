package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Passwords;

// TODO gestion animation pour éviter refresh tous les ticks
// TODO gestion multi scoreboard
public class Scoreboard {

	class Line<T extends OlympaPlayer> {
		ScoreboardLine<T> param;
		int timeLeft = 1;
		int lastAmount = 0;
		private List<VirtualTeam> teams = new ArrayList<>();

		Line(ScoreboardLine<T> param) {
			this.param = param;
			timeLeft = param.refresh;
		}

		/*public int firstLineIndex() {
			return sb.getTeamLine(teams.get(0));
		}*/

		@SuppressWarnings("unchecked")
		public String getValue() {
			return param.getValue((T) p);
		}

		/*public int lastLineIndex() {
			return sb.getTeamLine(teams.get(teams.size() - 1));
		}

		/**
		 * Refresh all lines, based on the first index of the previous lines
		 */
		/*public void refreshLines() {
			setLines(firstLineIndex());
		}

		public void removeLines() {
			int index = firstLineIndex();
			for (int i = 0; i < teams.size(); i++) {
				sb.removeLine(index);
				teams.remove(0);
			}
		}*/

		void reset() {
			teams.clear();
			lastAmount = 0;
			timeLeft = param.refresh;
		}

		/**
		 * How it works:
		 * <ol>
		 * <li> If there is no custom value, the default text will be used
		 * <li> If there is quests placeholders (<code>{questName}</code> or <code>{questDescription}</code>) they will be replaced by the appropriated value
		 * <li> All other placeholders are replaced
		 * <li> The final value is split into lines, depending of its length
		 * <li> If there is less lines than the previous time, theses lines are removed
		 * <li> If there is more lines than the previous time, all lines up are moved forward
		 * <li> Finally, the lines are set in the scoreboard
		 * </ol>
		 * @param firstLine Scoreboard line where the first line will be placed
		 */
		/*@SuppressWarnings("unchecked")
		public void setLines(int firstLine) {
			String text = param.getValue((T) p);
			text = ColorUtils.color(text);
			List<String> ls = Utils.splitOnSpace(text, param.length == 0 ? 48 : param.length);
		//			if (lastAmount > ls.size()) {
		//				int toRemove = lastAmount - ls.size();
		//				for (int i = 0; i < toRemove; i++) {
		//					System.out.println("ls " + firstLine + " remove team '" + teams.get(0).getValue() + "'");
		//					sb.removeLine(sb.getTeamLine(teams.get(0)));
		//					teams.remove(0);
		//				}
		//			} else if (lastAmount < ls.size()) {
		//				sb.moveLines(firstLine + lastAmount, ls.size() - lastAmount);
		//				System.out.println("ls " + firstLine + " moveLines '" + (firstLine + lastAmount) + "' to " + (ls.size() - lastAmount) + "'");
		//			}
			lastAmount = ls.size();
			for (int i = 0; i < ls.size(); i++) {
				String lineText = ls.get(i);
				if (lineText.length() > 48) {
					lineText = lineText.substring(0, 48);
				}
				System.out.println("sb setLine" + firstLine + " setLine '" + (firstLine + i) + "' to '" + lineText + "'");
				setTeam(i, sb.setLine(firstLine + i, lineText));
			}
		}*/

		/*private void setTeam(int index, VirtualTeam team) {
			if (teams.size() <= index) {
				teams.add(team); // theorically useless (space should be made before with sb.moveLines)
			} else {
				teams.set(index, team);
			}
		}
		
		private boolean tryRefresh() {
			if (param.refresh == 0) {
				return false;
			}
			timeLeft--;
			if (timeLeft == 0) {
				timeLeft = param.refresh;
				return true;
			}
			return false;
		}*/

	}

	private OlympaPlayer p;

	private ScoreboardManager manager;
	private ScoreboardSigns sb;
	private LinkedList<Line<?>> lines = new LinkedList<>();

	private BukkitRunnable runnable;

	private int animationSize = 1;

	Scoreboard(OlympaPlayer player, ScoreboardManager manager) {
		p = player;
		this.manager = manager;
		for (ScoreboardLine<?> line : manager.lines) {
			lines.add(new Line<>(line));
		}
		initScoreboard();
		launchTask();
	}

	public void addLine(ScoreboardLine<?> line) {
		Line<?> sline = new Line<>(line);
		lines.add(sline);
		// TODO update uniquement la ligné ajouté
		updateScoreboard();
	}

	public ScoreboardSigns getScoreboard() {
		return sb;
	}

	private BukkitRunnable getTask() {
		return new BukkitRunnable() {
			// nombre de changement dans l'animation -> TODO modifier pour plus de souplesse
			int i = 0;

			@Override
			public void run() {
				if (p.getPlayer() == null) {
					return;
				}
				updateScoreboard();
				if (++i >= animationSize) {
					cancel();
					runnable = getTask();
					runnable.runTaskTimerAsynchronously(manager.plugin, 10 * 20L, 1L);
				}
			}
		};
	}

	public void initScoreboard() {
		sb = new ScoreboardSigns(p.getPlayer(), manager.displayName, Passwords.generateRandomPassword(16), manager.lines.size());
		sb.create();
		for (int i = 0; i < lines.size(); i++) {
			Line<?> line = lines.get(i);
			String value = line.getValue();
			sb.setLine(i, value);
		}
		sb.display();
	}

	private void launchTask() {
		runnable = getTask();
		runnable.runTaskTimerAsynchronously(manager.plugin, 20L, 1L);
	}

	public void unload() {
		if (sb != null) {
			sb.destroy();
		}
		if (runnable != null) {
			runnable.cancel();
		}
	}

	public void updateScoreboard() {
		String oldName = new String(sb.objectiveName);
		String name;
		do {
			name = Passwords.generateRandomPassword(16);
		} while (name.equalsIgnoreCase(oldName));
		sb.objectiveName = name;
		sb.oldLines.addAll(sb.lines);
		sb.lines.clear();
		sb.created = false;
		sb.create();
		for (int i = 0; i < lines.size(); i++) {
			Line<?> line = lines.get(i);
			if (line.param instanceof AnimLine) {
				int newAnimSize = ((AnimLine) line.param).getAnimSize();
				if (newAnimSize > animationSize) {
					animationSize = newAnimSize;
				}
			}
			String value = line.getValue();
			sb.setLine(i, value);
		}
		sb.display();
		sb.destroy(oldName);
		sb.destroyTeam(sb.oldLines);
		sb.oldLines.clear();
	}

}