package fr.olympa.api.spigot.scoreboard.sign;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.bukkit.entity.Player;

import fr.olympa.api.common.player.OlympaPlayer;
import fr.olympa.api.spigot.lines.AbstractLine;
import fr.olympa.api.spigot.lines.LinesHolder;

// TODO gestion multi scoreboard
public class Scoreboard<T extends OlympaPlayer> extends Thread implements LinesHolder<Scoreboard<T>> {
	
	private static final long PAUSE_TIME = 5000;
	private static final long SCROLL_TIME = 200;
	
	private final T p;
	
	private ScoreboardManager<T> manager;
	private FastBoard sb;
	private LinkedList<Line> lines = new LinkedList<>();
	private LinkedList<Line> footers = new LinkedList<>();

	private Lock lock = new ReentrantLock();
	private Condition condition = lock.newCondition();
	
	private ReadWriteLock linesLock = new ReentrantReadWriteLock();
	
	private boolean willScroll;
	private int position;
	private boolean goDown;
	private Date nextUpdate = new Date();
	private int maxLine;
	private int linesSize;
	
	Scoreboard(T player, ScoreboardManager<T> manager) {
		super("Scoreboard " + player.getName());
		p = player;
		this.manager = manager;
		for (AbstractLine<Scoreboard<T>> line : manager.lines) {
			lines.add(new Line(line));
			line.addHolder(this);
		}
		for (AbstractLine<Scoreboard<T>> line : manager.footer) {
			footers.add(new Line(line));
			line.addHolder(this);
		}
	}
	
	public T getOlympaPlayer() {
		return p;
	}
	
	public FastBoard getScoreboard() {
		return sb;
	}
	
	@Deprecated (forRemoval = true)
	public void addLine(AbstractLine<Scoreboard<T>> absline) {
		addLines(absline);
	}
	
	public void addLines(AbstractLine<Scoreboard<T>>... abslines) { // can be called before scoreboard load
		if (sb != null && sb.isDeleted()) return;
		linesLock.writeLock().lock();
		for (AbstractLine<Scoreboard<T>> line : abslines) {
			lines.add(new Line(line));
			line.addHolder(this);
		}
		if (sb != null) updateScrollState();
		linesLock.writeLock().unlock();
		if (sb != null) needsUpdate();
	}
	
	@Override
	public synchronized void start() {
		updateScrollState();
		initScoreboard();
		super.start();
	}
	
	private void updateScrollState() {
		linesSize = lines.stream().mapToInt(x -> x.getLines(p).length).sum();
		if (linesSize > 15 - footers.size()) {
			if (!willScroll) {
				position = 0;
				willScroll = true;
				goDown = true;
				setNextUpdate(PAUSE_TIME);
			}
			maxLine = Math.min(linesSize, 15 - footers.size());
		}else {
			willScroll = false;
			position = 0;
			maxLine = linesSize;
		}
	}
	
	private void setNextUpdate(long time) {
		nextUpdate.setTime(System.currentTimeMillis() + time);
	}
	
	@Override
	public void update(AbstractLine<Scoreboard<T>> line, String newValue) {
		linesLock.readLock().lock();
		for (int i = 0; i < lines.size() + footers.size(); i++) {
			Line internalLine = i < lines.size() ? lines.get(i) : footers.get(i - lines.size());
			if (internalLine.line == line) {
				internalLine.setLines(newValue);
				break;
			}
		}
		updateScrollState();
		linesLock.readLock().unlock();
		needsUpdate();
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
				if (willScroll) {
					if (!condition.awaitUntil(nextUpdate)) { // le temps s'est ??coul??
						if (goDown) {
							position++;
							if (linesSize - position < 15 - footers.size()) {
								position = linesSize - 15 + footers.size();
								goDown = false;
								setNextUpdate(PAUSE_TIME);
							}else setNextUpdate(SCROLL_TIME);
						}else {
							position--;
							if (position < 0) {
								position = 0;
								goDown = true;
								setNextUpdate(PAUSE_TIME);
							}else setNextUpdate(SCROLL_TIME);
						}
					}
				}else condition.await();
				updateScoreboard();
			} catch (InterruptedException e) {
				interrupt();
				break;
			} finally {
				lock.unlock();
			}
		}
	}
	
	private void initScoreboard() {
		sb = new FastBoard((Player) p.getPlayer());
		sb.updateTitle(manager.displayName);
		sb.updateLines(getRawLines());
	}
	
	private List<String> getRawLines() {
		List<String> rawLines = new ArrayList<>(lines.size());
		int linePosition = 0;
		lines: for (int i = 0; i < lines.size(); i++) {
			Line line = lines.get(i);
			for (String internalLine : line.getLines(p)) {
				if (linePosition++ >= position) {
					if (linePosition >= maxLine + position) {
						if (!internalLine.isEmpty()) rawLines.add(internalLine);
						break lines;
					}else rawLines.add(internalLine);
				}
			}
		}
		for (Line footer : footers) {
			for (String internalLine : footer.getLines(p)) rawLines.add(internalLine);
		}
		return rawLines;
	}
	
	public void unload() {
		interrupt();
		if (sb != null) sb.delete();
		manager.getPlugin().sendMessage("D??chargement de ??6%d lignes ??epour le joueur ??6%s", lines.size(), p.getName());
		linesLock.writeLock().lock();
		for (Iterator<Scoreboard<T>.Line> iterator = lines.iterator(); iterator.hasNext();) {
			iterator.next().line.removeHolder(this);
			iterator.remove();
		}
		linesLock.writeLock().unlock();
	}
	
	private void updateScoreboard() {
		if (sb == null || sb.isDeleted()) return;
		
		linesLock.readLock().lock();
		List<String> rawLines = getRawLines();
		linesLock.readLock().unlock();
		
		sb.updateLines(rawLines);
	}
	
	class Line {
		AbstractLine<Scoreboard<T>> line;
		String[] cachedLines = null;
		
		public Line(AbstractLine<Scoreboard<T>> line) {
			this.line = line;
		}
		
		public void updateLines(T player) {
			setLines(line.getValue(Scoreboard.this));
		}
		
		public void setLines(String text) {
			cachedLines = text.split("\\n");
		}
		
		public String[] getLines(T player) {
			if (cachedLines == null) updateLines(player);
			return cachedLines;
		}
		
	}
	
}