package fr.olympa.api.utils;

import java.lang.reflect.Field;
import java.util.Collection;

import org.bukkit.craftbukkit.v1_15_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import net.minecraft.server.v1_15_R1.Packet;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_15_R1.ScoreboardTeam;

public class NMS {

	public static PacketPlayOutScoreboardTeam addPlayersToTeam(ScoreboardTeam team, Collection<String> who) {
		if (who.isEmpty()) {
			return null;
		}
		return new PacketPlayOutScoreboardTeam(team, who, 3);
	}

	public static ScoreboardTeam getNMSTeam(Team team) throws ReflectiveOperationException {
		Field field = team.getClass().getDeclaredField("team");
		field.setAccessible(true);
		return (ScoreboardTeam) field.get(team);
	}

	public static PacketPlayOutScoreboardTeam removePlayersFromTeam(ScoreboardTeam team, Collection<String> who) {
		if (who.isEmpty()) {
			return null;
		}
		return new PacketPlayOutScoreboardTeam(team, who, 4);
	}

	public static void sendPacket(Packet<?> packet, Player... players) {
		if (packet == null) {
			return;
		}
		for (Player p : players) {
			if (p != null) {
				((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
			}
		}
	}

}