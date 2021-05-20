package fr.olympa.api.scoreboard.tab;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

import fr.olympa.api.utils.Reflection;
import net.minecraft.server.v1_16_R3.ChatComponentText;
import net.minecraft.server.v1_16_R3.EnumChatFormat;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.Packet;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_16_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_16_R3.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_16_R3.PlayerConnection;

public class TabManager implements Listener {
	
	private DecimalFormat format = new DecimalFormat("00");
	
	private Map<Integer, String> texts = new HashMap<>(20);
	
	private List<Packet<?>> teamPackets = new ArrayList<>(4);
	private List<FakePlayer> fakePlayers = new ArrayList<>(80);
	
	private Plugin plugin;
	
	public TabManager(Plugin plugin) {
		this.plugin = plugin;
	}
	
	public TabManager addText(int slot, String text) {
		Validate.isTrue(teamPackets.isEmpty(), "Already built");
		Validate.isTrue(slot >= 0 && slot < 40, "Slot must be bound into 0 - 39");
		texts.put(slot, text);
		return this;
	}
	
	public TabManager build() {
		String randomized = Integer.toString(Math.abs(ThreadLocalRandom.current().nextInt()));
		
		List<String> players = new ArrayList<>(20);
		for (int i = 0; i < 20; i++) {
			String playerName = randomized + "é" + format.format(i);
			fakePlayers.add(createPlayer(playerName, texts.containsKey(i) ? new ChatComponentText(texts.get(i)) : ChatComponentText.d));
			players.add(playerName);
		}
		createTeam("+00AAA" + randomized, players).forEach(teamPackets::add);
		
		players = new ArrayList<>(59);
		for (int i = 20; i < 80; i++) {
			String playerName = randomized + "é" + format.format(i);
			fakePlayers.add(createPlayer(playerName, i >= 60 && texts.containsKey(i - 40) ? new ChatComponentText(texts.get(i - 40)) : ChatComponentText.d));
			players.add(playerName);
		}
		createTeam("A__ZZA" + randomized, players).forEach(teamPackets::add);
		
		Bukkit.getPluginManager().registerEvents(this, plugin);
		
		return this;
	}

	private List<PacketPlayOutScoreboardTeam> createTeam(String teamName, List<String> players) {
		Validate.isTrue(teamName.length() <= 16, "Team " + teamName + " length greater than 16 characters");
		PacketPlayOutScoreboardTeam packetTeam = new PacketPlayOutScoreboardTeam();
		Reflection.setFieldValue(packetTeam, "a", teamName);
		Reflection.setFieldValue(packetTeam, "e", "never");
		Reflection.setFieldValue(packetTeam, "f", "never");
		Reflection.setFieldValue(packetTeam, "g", EnumChatFormat.BLACK);
		if (players != null) {
			PacketPlayOutScoreboardTeam packetPlayers = new PacketPlayOutScoreboardTeam();
			Reflection.setFieldValue(packetPlayers, "i", 3);
			Reflection.setFieldValue(packetPlayers, "a", teamName);
			Reflection.setFieldValue(packetPlayers, "h", players);
			return Arrays.asList(packetTeam, packetPlayers);
		}
		return Arrays.asList(packetTeam);
	}
	
	private FakePlayer createPlayer(String playerName, IChatBaseComponent component) {
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), playerName);
		gameProfile.getProperties().put("textures", new Property("textures", "eyJ0aW1lc3RhbXAiOjE1MzgwNTAyNDE2NzEsInByb2ZpbGVJZCI6ImVmYWY1NzU3NzgxZTQ3YWViYzE0Y2Q4MmM5MWM3ZjgyIiwicHJvZmlsZU5hbWUiOiJNaW5lU2tpbiIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjJiZTU0ZGNlNWVhODEyNzg4MjM1ZWFkNDE4MGEwNTQ3ZmQ1Yjk4NmZjYjUwZjhkOWUxNDMxZGY2Y2Q0NjJkMCJ9fX0=", "VqaUy1zG0gzt81Hk7ihD1eC7NDaCh0mk0pBOVAEqrtqpg+QozlxFPkE0oHpf4+CIng3NxK7jvYBY3FjvjVxwAy70DYUqZUA3RdC63c/OtWC2qqVE+r65Z/DwpuWfMjxkyUsbKw6WxJXVLfHqir4AfULGDm5/43EPUJb6JAjj+iin5ladMY//kdliI4Tqe5ivDSCrO+4vHEItR0q9nz3djcL8399GaGLXwYe70+Fevw1VfDEGS8ubtN0h7KfXGxBAoMbwS2kY7D71foDU+6Z4QxEcKBkmznUunib+7gYeLJNPWXhxo2fMtDbAi3fH98/YzXIsnA0aOO6WOsme7fJDcU/y+/jsxpDyuhXS4jKsJcYx1oLt14JvmMn3Jt09L8vnEOnM+oVwSaYxTuzNhKlvorI27opACeEuXIIFg4oysuNkw1/KMEku274prIce1HJWwuQdJLvejQFUFYr7JJnEW1y8cT+Z7Q0R2sDCfLlk+vSv9aHoTLYE9IjtJen2yW1z6lRuzl0gJNUjs6T5HjOub2CcI7zHLUNum2kIiYYLLMg8tPU0TIG5WO2RrYWAEJLEOXma+1VcXqygpYBm9R86EBOxoTNN1dZVNKh4/DwY97zUGCcK2sNst5WUTR3Q+nE0C+TD66Qf9RPMG83sAqgNzKGcjIy4nOu4yud5M/0JVmY="));
		
		PacketPlayOutPlayerInfo packetInfoCreate = new PacketPlayOutPlayerInfo();
		Reflection.setFieldValue(packetInfoCreate, "a", EnumPlayerInfoAction.ADD_PLAYER);
		Reflection.setFieldValue(packetInfoCreate, "b", Arrays.asList(packetInfoCreate.new PlayerInfoData(gameProfile, 0, EnumGamemode.ADVENTURE, component)));
		
		PacketPlayOutPlayerInfo packetInfoRemove = new PacketPlayOutPlayerInfo();
		Reflection.setFieldValue(packetInfoRemove, "a", EnumPlayerInfoAction.REMOVE_PLAYER);
		Reflection.setFieldValue(packetInfoRemove, "b", Arrays.asList(packetInfoRemove.new PlayerInfoData(gameProfile, 0, EnumGamemode.ADVENTURE, component)));
		
		return new FakePlayer(gameProfile.getId(), packetInfoCreate, packetInfoRemove);
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		
		PlayerConnection playerConnection = ((CraftPlayer) p).getHandle().playerConnection;
		teamPackets.forEach(playerConnection::sendPacket);
		
		int online = Bukkit.getOnlinePlayers().size();
		List<Packet<?>> globalPackets = new ArrayList<>();
		if (online >= 60) {
			if (online == 60) { // = on vient de dépasser la limite, il faut remove le reste des fake players
				for (int i = 0; i < 20; i++) {
					globalPackets.add(fakePlayers.get(i).removePacket);
				}
			}
		}else if (online >= 40) {
			if (online == 40) {
				for (int i = 60; i < 80; i++) {
					globalPackets.add(fakePlayers.get(i).removePacket);
				}
			}else {
				for (int i = 0; i < 20; i++) playerConnection.sendPacket(fakePlayers.get(i).createPacket);
			}
		}else {
			globalPackets.add(fakePlayers.get(19 + online).removePacket);
			for (int i = 0; i < 80; i++) {
				if (!(i > 19 && i < 20 + online)) playerConnection.sendPacket(fakePlayers.get(i).createPacket);
			}
		}
		if (!globalPackets.isEmpty()) {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer == p) continue;
				playerConnection = ((CraftPlayer) onlinePlayer).getHandle().playerConnection;
				globalPackets.forEach(playerConnection::sendPacket);
			}
		}
	}
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		int online = Bukkit.getOnlinePlayers().size() - 1;
		if (online == 0) return;
		
		List<Packet<?>> globalPackets = new ArrayList<>();
		if (online == 40) {
			for (int i = 60; i < 80; i++) {
				globalPackets.add(fakePlayers.get(i).createPacket);
			}
		}else if (online == 60) {
			for (int i = 0; i < 20; i++) {
				globalPackets.add(fakePlayers.get(i).createPacket);
			}
		}else {
			globalPackets.add(fakePlayers.get(20 + online).createPacket);
		}
		
		if (!globalPackets.isEmpty()) {
			for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
				if (onlinePlayer == e.getPlayer()) continue;
				PlayerConnection playerConnection = ((CraftPlayer) onlinePlayer).getHandle().playerConnection;
				globalPackets.forEach(playerConnection::sendPacket);
			}
		}
	}
	
	class FakePlayer {
		UUID uuid;
		PacketPlayOutPlayerInfo createPacket;
		PacketPlayOutPlayerInfo removePacket;
		
		public FakePlayer(UUID uuid, PacketPlayOutPlayerInfo createPacket, PacketPlayOutPlayerInfo removePacket) {
			this.uuid = uuid;
			this.createPacket = createPacket;
			this.removePacket = removePacket;
		}
	}
	
}
