package fr.olympa.api.utils;

import java.lang.reflect.Field;
import java.util.Arrays;

import org.bukkit.craftbukkit.v1_13_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import net.minecraft.server.v1_13_R2.Packet;
import net.minecraft.server.v1_13_R2.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_13_R2.ScoreboardTeam;

public class NMS {

	public static void sendPacket(Player p, Packet<?> packet) {
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(packet);
	}

	public static ScoreboardTeam getNMSTeam(Team team) throws ReflectiveOperationException {
		Field field = team.getClass().getDeclaredField("team");
		field.setAccessible(true);
		return (ScoreboardTeam) field.get(team);
	}

	public static void addPlayerToTeam(String who, Player viewer, ScoreboardTeam team) {
		sendPacket(viewer, new PacketPlayOutScoreboardTeam(team, Arrays.asList(who), 3));
	}

}
