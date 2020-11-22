package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.google.common.collect.Iterables;

import fr.olympa.api.lines.AbstractLine;
import fr.olympa.api.lines.LinesHolder;
import fr.olympa.api.player.OlympaPlayer;

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
	
	private Lock updateLock = new ReentrantLock();
	
	private boolean willScroll;
	private int position;
	private boolean goDown;
	private Date nextUpdate;
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
		updateScrollState();
		initScoreboard();
		start();
	}
	
	public T getOlympaPlayer() {
		return p;
	}
	
	public FastBoard getScoreboard() {
		return sb;
	}
	
	public void addLine(AbstractLine<Scoreboard<T>> line) {
		updateLock.lock();
		lines.add(new Line(line));
		line.addHolder(this);
		updateScrollState();
		updateLock.unlock();
		needsUpdate();
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
		nextUpdate = new Date(System.currentTimeMillis() + time);
	}
	
	@Override
	public void update(AbstractLine<Scoreboard<T>> line, String newValue) {
		for (Line internalLine : Iterables.concat(lines, footers)) {
			if (internalLine.line == line) {
				internalLine.setLines(newValue);
				break;
			}
		}
		updateScrollState();
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
					if (!condition.awaitUntil(nextUpdate)) { // le temps s'est écoulé
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
				//OlympaCore.getInstance().sendMessage("Boucle du scoreboard de " + p.getName() + " interrompue.");
				return;
			} finally {
				lock.unlock();
			}
		}
	}
	
	private void initScoreboard() {
		sb = new FastBoard(p.getPlayer());
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
		for (Iterator<Scoreboard<T>.Line> iterator = lines.iterator(); iterator.hasNext();) {
			iterator.next().line.removeHolder(this);
			iterator.remove();
		}
	}
	
	private void updateScoreboard() {
		updateLock.lock();
		List<String> rawLines = getRawLines();
		updateLock.unlock();
		
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
			//return ChatPaginator.wordWrap(text, 48);
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