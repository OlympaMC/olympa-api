package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Passwords;
import fr.olympa.api.utils.Utils;

public class Scoreboard {

	class Line<T extends OlympaPlayer> {
		ScoreboardLine<T> param;
		int timeLeft = 1;
		int lastAmount = 0;
		List<VirtualTeam> teams = new ArrayList<>();

		Line(ScoreboardLine<T> param) {
			this.param = param;
			timeLeft = param.refresh;
		}

		public int firstLineIndex() {
			return sb.getTeamLine(teams.get(0));
		}

		public int lastLineIndex() {
			return sb.getTeamLine(teams.get(teams.size() - 1));
		}

		/**
		 * Refresh all lines, based on the first index of the previous lines
		 */
		public void refreshLines() {
			setLines(firstLineIndex());
		}

		public void removeLines() {
			int index = firstLineIndex();
			for (int i = 0; i < teams.size(); i++) {
				sb.removeLine(index);
				teams.remove(0);
			}
		}

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
		@SuppressWarnings("unchecked")
		public void setLines(int firstLine) {
			String text = param.getValue((T) p);
			text = ColorUtils.color(text);
			List<String> ls = Utils.splitOnSpace(text, param.length == 0 ? 48 : param.length);
			if (lastAmount > ls.size()) {
				int toRemove = lastAmount - ls.size();
				for (int i = 0; i < toRemove; i++) {
					sb.removeLine(sb.getTeamLine(teams.get(0)));
					teams.remove(0);
				}
			} else if (lastAmount < ls.size()) {
				sb.moveLines(firstLine + lastAmount, ls.size() - lastAmount);
			}
			lastAmount = ls.size();
			for (int i = 0; i < ls.size(); i++) {
				String lineText = ls.get(i);
				if (lineText.length() > 48) {
					lineText = lineText.substring(0, 48);
				}
				setTeam(i, sb.setLine(firstLine + i, lineText));
			}
		}

		private void setTeam(int index, VirtualTeam team) {
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
		}

	}

	private OlympaPlayer p;

	private ScoreboardManager manager;
	private ScoreboardSigns oldSb;
	private ScoreboardSigns sb;
	private LinkedList<Line<?>> lines = new LinkedList<>();

	private BukkitRunnable runnable;

	Scoreboard(OlympaPlayer player, ScoreboardManager manager) {
		p = player;
		this.manager = manager;

		for (ScoreboardLine<?> line : manager.lines) {
			lines.add(new Line<>(line));
		}

		initScoreboard();

		runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (p.getPlayer() == null) {
					return;
				}
				oldSb = sb;
				sb = oldSb.clone();
				String objectiveName;
				do {
					objectiveName = Passwords.generateRandomPassword(16);
				} while (oldSb.objectiveName.equalsIgnoreCase(objectiveName));
				sb.changeObjectiveName(objectiveName);
				sb.create();
				for (int i = 0; i < lines.size(); i++) {
					Line<?> line = lines.get(i);
					line.setLines(i == 0 ? 0 : lines.get(i - 1).lastLineIndex() + 1);
				}
				sb.sendLines();
				sb.display();
				oldSb.destroy();
				oldSb = null;

			}
		};
		runnable.runTaskTimerAsynchronously(manager.plugin, 20L, 20L);
		/*runnable = new BukkitRunnable() {
			@Override
			public void run() {
				if (p.getPlayer() == null) {
					return;
				}
				oldSb = sb;
				sb = oldSb.clone();
				String objectiveName;
				do {
					objectiveName = Passwords.generateRandomPassword(16);
				} while (oldSb.objectiveName.equalsIgnoreCase(objectiveName));
				sb.changeObjectiveName(objectiveName);
				sb.create();
		//				for (Line<?> line : lines) {
		//					if (line.tryRefresh()) {
		//						line.refreshLines();
		//					}
		//				}
				sb.sendLines();
				for (int i = 0; i < lines.size(); i++) {
					Line<?> line = lines.get(i);
					line.setLines(i == 0 ? 0 : lines.get(i - 1).lastLineIndex() + 1);
				}
				Bukkit.getScheduler().runTaskLaterAsynchronously(manager.plugin, () -> {
					//sb.setLine(lines.size() - 1, animation.get(i++));
					sb.display();
					//oldSb.remove();
					oldSb.remove();
					oldSb.destroy();
				}, 1L);
			}
		};
		runnable.runTaskTimerAsynchronously(manager.plugin, 20L, 20L);*/
	}

	public void addLine(ScoreboardLine<?> line) {
		Line<?> sline = new Line<>(line);
		lines.add(sline);
		int lastLineIndex = 0;
		if (lines.size() != 1) {
			lastLineIndex = lines.get(lines.size() - 2).lastLineIndex() + 1;
		}
		sline.setLines(lastLineIndex);
	}

	public ScoreboardSigns getScoreboard() {
		return sb;
	}

	public void initScoreboard() {
		sb = new ScoreboardSigns(p.getPlayer(), manager.displayName, Passwords.generateRandomPassword(16));
		sb.create();
		sb.sendLines();
		for (int i = 0; i < lines.size(); i++) {
			Line<?> line = lines.get(i);
			line.setLines(i == 0 ? 0 : lines.get(i - 1).lastLineIndex() + 1);
		}
		sb.display();
	}

	public void unload() {
		if (sb != null) {
			sb.destroy();
		}
		if (runnable != null) {
			runnable.cancel();
		}
	}

}