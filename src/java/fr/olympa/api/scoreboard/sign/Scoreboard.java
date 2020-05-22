package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.scoreboard.sign.lines.ScoreboardLine;
import fr.olympa.api.utils.Passwords;

// TODO gestion animation pour éviter refresh tous les ticks
// TODO gestion multi scoreboard
public class Scoreboard<T extends OlympaPlayer> extends Thread {

	public final T p;

	private ScoreboardManager<T> manager;
	private ScoreboardSigns sb;
	private LinkedList<ScoreboardLine<T>> lines = new LinkedList<>();
	
	private Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();

	Scoreboard(T player, ScoreboardManager<T> manager) {
		p = player;
		this.manager = manager;
		for (ScoreboardLine<T> line : manager.lines) {
			lines.add(line);
			line.addScoreboard(this);
		}
		for (ScoreboardLine<T> line : manager.footer) {
			lines.add(line);
			line.addScoreboard(this);
		}
		initScoreboard();
		start();
	}

	public void addLine(ScoreboardLine<T> line) {
		lines.add(lines.size() - manager.footer.size(), line);
		// TODO update uniquement la ligné ajouté
		needsUpdate();
	}

	public ScoreboardSigns getScoreboard() {
		return sb;
	}

	public synchronized void needsUpdate() {
		condition.signal();
	}

	@Override
	public void run() {
		while (true) {
			try {
				condition.await();
				updateScoreboard();
			}catch (InterruptedException e) {
				break;
			}
		}
	}

	public void initScoreboard() {
		sb = new ScoreboardSigns(p.getPlayer(), manager.displayName, Passwords.generateRandomPassword(16), manager.lines.size());
		sb.create();
		for (int i = 0; i < lines.size(); i++) {
			ScoreboardLine<T> line = lines.get(i);
			String value = line.getValue(p);
			sb.setLine(i, value);
		}
		sb.display();
	}

	public void unload() {
		interrupt();
		if (sb != null) sb.destroy();
		for (ScoreboardLine<T> line : lines) {
			line.removeScoreboard(this);
		}
		lines.clear();
	}

	public void updateScoreboard() {
		String oldName = new String(sb.objectiveName);
		String name;
		do {
			name = Passwords.generateRandomPassword(16);
		} while (name.equalsIgnoreCase(oldName));
		sb.objectiveName = name;
		sb.oldLines = (ArrayList<VirtualTeam>) sb.lines.clone();
		sb.lines.clear();
		sb.created = false;
		sb.create();
		for (int i = 0; i < lines.size(); i++) {
			String value = lines.get(i).getValue(p);
			sb.setLine(i, value);
		}
		sb.display();
		sb.destroy(oldName);
		sb.destroyTeam(sb.oldLines);
		sb.oldLines.clear();
	}

}