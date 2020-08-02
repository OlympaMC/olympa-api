package fr.olympa.api.scoreboard.sign;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Reflection;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.IScoreboardCriteria.EnumScoreboardHealthDisplay;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardDisplayObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardObjective;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_15_R1.ScoreboardServer.Action;

public class ScoreboardSigns implements Cloneable {

	protected String objectiveName;
	protected boolean created;
	protected ArrayList<VirtualTeam> lines = new ArrayList<>();
	protected ArrayList<VirtualTeam> oldLines = new ArrayList<>();
	private final Player player;
	protected int maxSize = 0;
	private String displayName;

	public ScoreboardSigns(Player player, String displayName, String objectiveName, int maxSize) {
		this.player = player;
		this.displayName = displayName;
		this.objectiveName = objectiveName.length() > 16 ? objectiveName.substring(0, 16) : objectiveName;
		this.maxSize = maxSize;
	}

	public void changeDisplayName(String name) throws ClassNotFoundException {
		objectiveName = name;
		if (created)
			Reflection.sendPacket(player, createObjectivePacket(2, objectiveName));
	}

	@Override
	public ScoreboardSigns clone() {
		try {
			return (ScoreboardSigns) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
			return null;
		}
	}

	private boolean containsValue(String value) {
		return lines.stream().anyMatch(l -> l.getValue().equalsIgnoreCase(value));
	}

	public void create() {
		if (created)
			return;
		Reflection.sendPacket(player, createObjectivePacket(0, objectiveName));
		created = true;
	}

	private Object createObjectivePacket(int mode, String objectiveName) {
		Object packet = new PacketPlayOutScoreboardObjective();
		Reflection.setField(packet, "a", objectiveName);
		Reflection.setField(packet, "d", mode);

		if (mode == 0 || mode == 2) {
			Reflection.setField(packet, "b", new ChatComponentText(displayName));
			Reflection.setField(packet, "c", EnumScoreboardHealthDisplay.INTEGER);
		}
		return packet;
	}

	public void destroy() {
		if (!created)
			destroy(objectiveName);
		destroyTeam(lines);
		created = false;
	}

	public void destroy(String objectiveName) {
		if (!created) {
			Reflection.sendPacket(player, createObjectivePacket(1, objectiveName));
			created = false;
		}
	}

	public void destroyTeam(List<VirtualTeam> teams) {
		for (VirtualTeam team : teams)
			if (team != null)
				Reflection.sendPacket(player, team.removeTeam());
	}

	public void display() {
		if (!created)
			return;
		Reflection.sendPacket(player, setObjectiveSlot());
	}

	private VirtualTeam getOrCreateTeam(String value) {
		while (containsValue(value))
			value = value + "§r";
		String finalValue = value;
		Set<String> oldNames = oldLines.stream().map(t -> {
			if (t == null)
				System.out.println("team null");
			else if (t.getName() == null)
				System.out.println("name null");
			return t.getName().toLowerCase();
		}).collect(Collectors.toSet());
		Set<String> actualNames = lines.stream().map(t -> t.getName().toLowerCase()).collect(Collectors.toSet());
		VirtualTeam team = oldLines.stream().filter(t -> t.getValue().equals(finalValue)).findFirst().orElse(null);
		String teamName;
		do
			teamName = SbUtils.generateRandomPassword(16);
		while (actualNames.contains(teamName.toLowerCase()) && oldNames.contains(teamName.toLowerCase()));
		if (team == null) {
			team = new VirtualTeam(teamName);
			team.setValue(finalValue);
			Set<String> actualValue = lines.stream().map(t -> t.getCurrentPlayer().toLowerCase()).collect(Collectors.toSet());
			while (actualValue.contains(team.getCurrentPlayer().toLowerCase()))
				team.currentPlayer = team.getCurrentPlayer() + "§r";
			lines.add(team);
			if (lines.size() > maxSize)
				maxSize = lines.size();
			// == maxSize ++;
		} else {
			oldLines.remove(team);
			lines.add(team);
		}
		return team;
	}

	private int getScore(int i) {
		return maxSize - i;
	}

	private Object sendScore(int score, String teamPlayer) {
		Object packet = new PacketPlayOutScoreboardScore();
		Reflection.setField(packet, "a", teamPlayer);
		Reflection.setField(packet, "b", objectiveName);
		Reflection.setField(packet, "c", score);
		Reflection.setField(packet, "d", Action.CHANGE);
		return packet;
	}

	public void setLine(int line, String value) {
		if (line > 14 || line < 0 || !created)
			return;

		VirtualTeam team = getOrCreateTeam(value);
		for (Object packet : team.sendLine())
			Reflection.sendPacket(player, packet);
		Reflection.sendPacket(player, sendScore(getScore(line), team.getCurrentPlayer()));
		team.reset();
	}

	private Object setObjectiveSlot() {
		Object packet = new PacketPlayOutScoreboardDisplayObjective();
		Reflection.setField(packet, "a", 1);
		Reflection.setField(packet, "b", objectiveName);

		return packet;
	}
}