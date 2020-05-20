package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.LinkedList;

import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.api.utils.Passwords;

// TODO gestion animation pour éviter refresh tous les ticks
// TODO gestion multi scoreboard
public class Scoreboard<T extends OlympaPlayer> extends Thread {

	public final T p;

	private ScoreboardManager<T> manager;
	private ScoreboardSigns sb;
	private LinkedList<ScoreboardLine<T>> lines = new LinkedList<>();

	private int animationSize = 0;
	private int timeBetweenAnimations = 10 * 20;

	private boolean needsUpdate = false;

	int animStep = 0;
	int waitBeforeAnim = 0;

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

	public void needsUpdate() {
		needsUpdate = true;
	}

	@Override
	public void run() {
		while (true) {
			if (waitBeforeAnim-- < 0 || needsUpdate) {
				if (++animStep >= animationSize) {
					animStep = 0;
					waitBeforeAnim = timeBetweenAnimations;
				}
				needsUpdate = false;
				updateScoreboard();
			}
			try {
				Thread.sleep(50);
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
		long beg = System.currentTimeMillis();
		long pass, create, linesT, display, end;
		String oldName = new String(sb.objectiveName);
		String name;
		do {
			name = Passwords.generateRandomPassword(16);
		} while (name.equalsIgnoreCase(oldName));
		pass = System.currentTimeMillis();
		sb.objectiveName = name;
		sb.oldLines = (ArrayList<VirtualTeam>) sb.lines.clone();
		sb.lines.clear();
		sb.created = false;
		sb.create();
		create = System.currentTimeMillis();
		for (int i = 0; i < lines.size(); i++) {
			ScoreboardLine<T> line = lines.get(i);
			if (line instanceof AnimLine) {
				int newAnimSize = ((AnimLine) line).getAnimSize();
				if (newAnimSize > animationSize) {
					animationSize = newAnimSize;
				}
			}
			String value = line.getValue(p);
			sb.setLine(i, value);
		}
		linesT = System.currentTimeMillis();
		sb.display();
		display = System.currentTimeMillis();
		sb.destroy(oldName);
		sb.destroyTeam(sb.oldLines);
		sb.oldLines.clear();
		end = System.currentTimeMillis();
		System.out.println("Scoreboard.updateScoreboard() TIME " + (pass - beg) + " " + (create - beg) + " " + (linesT - beg) + " " + (display - beg) + " " + (end - beg));
	}

}