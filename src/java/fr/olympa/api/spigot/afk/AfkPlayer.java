package fr.olympa.api.spigot.afk;

import java.util.EnumMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import fr.olympa.api.common.provider.AccountProviderAPI;
import fr.olympa.api.spigot.customevents.AsyncPlayerAfkEvent;
import fr.olympa.api.spigot.scoreboard.tab.INametagApi;
import fr.olympa.api.utils.Prefix;
import fr.olympa.core.spigot.OlympaCore;
import net.minecraft.server.v1_16_R3.PacketPlayInArmAnimation;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockDig;
import net.minecraft.server.v1_16_R3.PacketPlayInBlockPlace;
import net.minecraft.server.v1_16_R3.PacketPlayInChat;
import net.minecraft.server.v1_16_R3.PacketPlayInFlying.PacketPlayInPosition;

public class AfkPlayer {

	public static final String AFK_SUFFIX = "§4[§cAFK§4]§r";

	private static final int scoreToActiveAfk = 10;
	private static final int scoreToDeactiveAfk = 6;
	private static final double minRatioToDetermineAfk = 0.6;

	private Player p;
	
	private int afkScore = 0;
	private boolean isAfk;
	private long startAfkTime;
	private BukkitRunnable task;

	private EnumMap<ListenedPacket, Integer> oldLog;
	private EnumMap<ListenedPacket, Integer> newLog;

	public AfkPlayer(Player player) {
		isAfk = false;
		this.p = player;
		
		oldLog = new EnumMap<>(ListenedPacket.class);
		newLog = new EnumMap<>(ListenedPacket.class);

		task = new BukkitRunnable() {

			@Override
			public void run() {
				if (ListenedPacket.getOutOfToleranceRatio(oldLog, newLog) >= minRatioToDetermineAfk)
					afkScore++;
				else
					afkScore -= 3;

				afkScore = Math.min(Math.max(afkScore, 0), scoreToActiveAfk);

				if (isAfk && afkScore < scoreToDeactiveAfk)
					setNotAfk();
				else if (!isAfk && afkScore >= scoreToActiveAfk)
					setAfk();

				oldLog = newLog;
				newLog = new EnumMap<>(ListenedPacket.class);
			}
		};

		task.runTaskTimerAsynchronously(OlympaCore.getInstance(), 1, 20 * 40);
	}

	/*public AfkPlayer(boolean afk, String lastAction) {
		this.lastAction = lastAction;
		this.afk = afk;
		time = Utils.getCurrentTimeInSeconds();
	}*/

	public void cancelTask() {
		task.cancel();
	}

	public void setAfk() {
		isAfk = true;
		startAfkTime = System.currentTimeMillis();
		afkScore = scoreToActiveAfk;
		
		Prefix.DEFAULT_BAD.sendMessage(p, "Tu es désormais &4AFK&c.");
		INametagApi api = OlympaCore.getInstance().getNameTagApi();
		if (api != null)
			api.callNametagUpdate(AccountProviderAPI.getter().get(p.getUniqueId()));

		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> Bukkit.getServer().getPluginManager().callEvent(new AsyncPlayerAfkEvent(p, isAfk)));
	}

	public void setNotAfk() {
		isAfk = false;
		afkScore = 0;
		
		Prefix.DEFAULT_GOOD.sendMessage(p, "Tu n'es plus &2AFK&a.");
		INametagApi api = OlympaCore.getInstance().getNameTagApi();
		if (api != null)
			api.callNametagUpdate(AccountProviderAPI.getter().get(p.getUniqueId()));
		
		OlympaCore.getInstance().getTask().runTaskAsynchronously(() -> Bukkit.getServer().getPluginManager().callEvent(new AsyncPlayerAfkEvent(p, isAfk)));
	}

	/*private String getLastSuffix() {
		return lastSuffix != null ? lastSuffix : " ";
	}*/

	public boolean isAfk() {
		return isAfk;
	}

	public long getTime() {
		return isAfk() ? System.currentTimeMillis() - startAfkTime : 0;
	}

	public void toggleAfk(Player player) {
		if (isAfk)
			setNotAfk();
		else
			setAfk();
	}

	public void addToLog(Object packet) {
		if (packet == null)
			return;

		ListenedPacket lp = ListenedPacket.get(packet.getClass());
		if (lp == null)
			return;

		if (newLog.containsKey(lp))
			newLog.put(lp, newLog.get(lp) + 1);
		else
			newLog.put(lp, 1);
	}

	private enum ListenedPacket {
		CHAT_PACKET(PacketPlayInChat.class, 0.2),
		BREAK_PACKET(PacketPlayInBlockDig.class, 0.15),
		PLACE_PACKET(PacketPlayInBlockPlace.class, 0.15),
		MOVE_PACKET(PacketPlayInPosition.class, 0.15),
		ARM_ANIMATION_PACKET(PacketPlayInArmAnimation.class, 0.1)

		;

		private Class<?> pClass;
		private double tol;

		ListenedPacket(Class<?> pClass, double tolerance) {
			this.pClass = pClass;
			tol = tolerance;
		}

		public Class<?> getPacketClass() {
			return pClass;
		}

		public double getToleranceRange() {
			return tol;
		}

		public static ListenedPacket get(Class<?> pClass) {
			for (ListenedPacket p : ListenedPacket.values())
				if (p.getPacketClass().equals(pClass))
					return p;

			return null;
		}

		public static double getOutOfToleranceRatio(Map<ListenedPacket, Integer> oldLog, Map<ListenedPacket, Integer> newLog) {
			double c = 0;
			double cMax = 0;

			for (ListenedPacket p : ListenedPacket.values())
				if (oldLog.containsKey(p) && newLog.containsKey(p)) {
					cMax++;

					if (oldLog.get(p) * (1 + p.getToleranceRange()) > newLog.get(p) &&
							oldLog.get(p) * (1 - p.getToleranceRange()) < newLog.get(p))
						c++;
				}

			return cMax == 0 ? 1 : c / cMax;
		}
	}
}
