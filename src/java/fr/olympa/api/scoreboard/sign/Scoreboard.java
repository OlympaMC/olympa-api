package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.LinesHolder;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.core.spigot.OlympaCore;

// TODO gestion animation pour éviter refresh tous les ticks
// TODO gestion multi scoreboard
public class Scoreboard<T extends OlympaPlayer> extends Thread implements LinesHolder<Scoreboard<T>> {
	
	private final T p;
	
	private ScoreboardManager<T> manager;
	private ScoreboardSigns sb;
	private LinkedList<Line> lines = new LinkedList<>();

	private Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	private Lock updateLock = new ReentrantLock();
	
	Scoreboard(T player, ScoreboardManager<T> manager) {
		super("Scoreboard " + player.getName());
		p = player;
		this.manager = manager;
		for (AbstractLine<Scoreboard<T>> line : manager.lines) {
			lines.add(new Line(line));
			line.addHolder(this);
		}
		for (AbstractLine<Scoreboard<T>> line : manager.footer) {
			lines.add(new Line(line));
			line.addHolder(this);
		}
		initScoreboard();
		start();
	}
	
	public T getOlympaPlayer() {
		return p;
	}
	
	public void addLine(AbstractLine<Scoreboard<T>> line) {
		updateLock.lock();
		lines.add(lines.size() - manager.footer.size(), new Line(line));
		updateLock.unlock();
		// TODO update uniquement la ligné ajouté
		needsUpdate();
	}
	
	public ScoreboardSigns getScoreboard() {
		return sb;
	}
	
	@Override
	public void update(AbstractLine<Scoreboard<T>> line) {
		needsUpdate(); // TODO update seulement la ligne
	}
	
	public void needsUpdate() {
		lock.lock();
		try {
			condition.signal();
		} finally {
			lock.unlock();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			lock.lock();
			try {
				condition.await();
				updateScoreboard();
			} catch (InterruptedException e) {
				OlympaCore.getInstance().sendMessage("Boucle du scoreboard de " + p.getName() + " interrompue.");
				return;
			} finally {
				lock.unlock();
			}
		}
	}
	
	private void initScoreboard() {
		sb = new ScoreboardSigns(p.getPlayer(), manager.displayName, SbUtils.generateRandomPassword(16), manager.lines.size());
		sb.create();
		int sbLine = 0;
		for (Line line : lines) {
			String[] value = line.getLines(p);
			for (String internalLine : value)
				sb.setLine(sbLine++, internalLine);
		}
		sb.display();
	}
	
	public void unload() {
		interrupt();
		if (sb != null) sb.destroy();
		for (Iterator<Scoreboard<T>.Line> iterator = lines.iterator(); iterator.hasNext();) {
			iterator.next().line.removeHolder(this);
			iterator.remove();
		}
	}
	
	private void updateScoreboard() {
		String oldName = new String(sb.objectiveName);
		String name;
		do
			name = SbUtils.generateRandomPassword(16);
		while (name.equalsIgnoreCase(oldName));
		sb.objectiveName = name;
		sb.oldLines = (ArrayList<VirtualTeam>) sb.lines.clone();
		sb.lines.clear();
		sb.created = false;
		sb.create();
		int sbLine = 0;
		updateLock.lock();
		for (Line line : lines) {
			String[] value = line.getLines(p);
			for (String internalLine : value)
				sb.setLine(sbLine++, internalLine);
		}
		updateLock.unlock();
		sb.display();
		sb.destroy(oldName);
		sb.destroyTeam(sb.oldLines);
		sb.oldLines.clear();
	}
	
	class Line {
		AbstractLine<Scoreboard<T>> line;
		
		public Line(AbstractLine<Scoreboard<T>> line) {
			this.line = line;
		}
		
		public String[] getLines(T player) {
			String text = line.getValue(Scoreboard.this);
			return text.split("\\n");
			//return ChatPaginator.wordWrap(text, 48);
		}
		
	}
	
}