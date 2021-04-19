package fr.olympa.api.utils.spigot;

import java.sql.SQLException;
import java.text.DecimalFormat;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import fr.olympa.api.holograms.Hologram;
import fr.olympa.api.holograms.Hologram.HologramLine;
import fr.olympa.api.lines.DynamicLine;
import fr.olympa.api.lines.FixedLine;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;

public abstract class AbstractRank {
	
	private static final DecimalFormat format = new DecimalFormat("0.##");
	
	protected final String id;
	protected final Hologram hologram;
	
	private DynamicLine<HologramLine>[] scoreLines = new DynamicLine[10];
	private ScoreEntry[] scores = new ScoreEntry[10];
	
	protected AbstractRank(String id, Location location) throws SQLException {
		this.id = id;
		
		for (int i = 0; i < scores.length; i++) scores[i] = new ScoreEntry();
		
		fillUpScores(scores);
		
		hologram = OlympaCore.getInstance().getHologramsManager().createHologram(location, false, true, new FixedLine<>(getHologramName()), FixedLine.EMPTY_LINE);
		for (int i = 0; i < scoreLines.length; i++) {
			int slot = i;
			DynamicLine<HologramLine> line = new DynamicLine<>(x -> scores[slot].toString());
			scoreLines[i] = line;
			hologram.addLine(scoreLines[i]);
		}
	}
	
	public final String getID() {
		return id;
	}
	
	public abstract String getHologramName();
	
	public abstract String getMessageName();
	
	public void handleNewScore(Player player, double scoreValue) {
		if (player == null) return;
		String name = player.getName();
		int oldSlot = -1;
		int newSlot = -1;
		
		for (int slot = 0; slot < scores.length; slot++) {
			ScoreEntry score = scores[slot];
			if (name.equals(score.name)) {
				if (score.score < scoreValue) {
					System.arraycopy(scores, slot + 1, scores, slot, scores.length - slot - 1); // supprimer cette entrée et tout décaler d'un cran à gauche
					scoreLines[slot].updateGlobal();
					oldSlot = slot;
				}
				break;
			}
		}
		
		boolean updated = false;
		for (int slot = 0; slot < scores.length; slot++) {
			if (updated) {
				scoreLines[slot].updateGlobal();
			}else {
				ScoreEntry score = scores[slot];
				if (score.score < scoreValue) {
					System.arraycopy(scores, slot, scores, slot + 1, scores.length - slot - 1);
					scores[slot] = new ScoreEntry(name, scoreValue);
					updated = true;
					scoreLines[slot].updateGlobal();
					newSlot = slot;
				}
			}
		}
		
		if (newSlot == -1) return;
		if (oldSlot == -1) {
			Prefix.DEFAULT_GOOD.sendMessage(player, "Félicitations ! Tu arrives dans le classement %s à la %de place !", getMessageName(), newSlot);
		}else Prefix.DEFAULT_GOOD.sendMessage(player, "Félicitations ! Tu progresses dans le classement %s de la %de à la %de place !", getMessageName(), oldSlot, newSlot);
	}
	
	protected abstract void fillUpScores(ScoreEntry[] scores) throws SQLException;
	
	public static class ScoreEntry {
		private String name;
		private double score;
		
		public ScoreEntry() {}
		
		public ScoreEntry(String name, double score) {
			fill(name, score);
		}
		
		public String getName() {
			return name;
		}
		
		public double getScore() {
			return score;
		}
		
		public void setScore(double score) {
			this.score = score;
		}
		
		public void fill(String name, double score) {
			this.name = name;
			this.score = score;
		}
		
		public String toString(int slot) {
			return slot + " - " + toString();
		}
		
		@Override
		public String toString() {
			return name == null ? "§ovide" : (name + " : " + format.format(score));
		}
		
	}
	
}
