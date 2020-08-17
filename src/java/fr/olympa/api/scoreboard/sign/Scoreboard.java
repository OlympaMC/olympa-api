package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.LinesHolder;
import fr.olympa.api.player.OlympaPlayer;
import fr.olympa.core.spigot.OlympaCore;

// TODO gestion multi scoreboard
public class Scoreboard<T extends OlympaPlayer> extends Thread implements LinesHolder<Scoreboard<T>> {
	
	private final T p;
	
	private ScoreboardManager<T> manager;
	private FastBoard sb;
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
		line.addHolder(this);
		updateLock.unlock();
		// TODO update uniquement la ligné ajouté
		needsUpdate();
	}
	
	public FastBoard getScoreboard() {
		return sb;
	}
	
	@Override
	public void update(AbstractLine<Scoreboard<T>> line, String newValue) {
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
		sb = new FastBoard(p.getPlayer());
		sb.updateTitle(manager.displayName);
		List<String> rawLines = new ArrayList<>();
		for (Line line : lines) {
			String[] value = line.getLines(p);
			for (String internalLine : value) rawLines.add(internalLine);
		}
		sb.updateLines(rawLines);
	}
	
	public void unload() {
		interrupt();
		if (sb != null) sb.delete();
		for (Iterator<Scoreboard<T>.Line> iterator = lines.iterator(); iterator.hasNext();) {
			iterator.next().line.removeHolder(this);
			iterator.remove();
		}
	}
	
	private void updateScoreboard() {
		updateLock.lock();
		List<String> strings = new ArrayList<>(lines.size());
		lines.forEach(x -> {
			for (String line : x.getLines(p)) {
				strings.add(line);
			}
		});
		updateLock.unlock();
		
		sb.updateLines(strings);
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