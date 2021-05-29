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
	
	public static final int PING_FULL = 0;
	public static final int PING_ONE_BAR = 1001;
	public static final int PING_ERROR = -1;
	
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
	
	public TabManager build() throws ReflectiveOperationException {
		String randomized = Integer.toString(Math.abs(ThreadLocalRandom.current().nextInt()));
		
		List<String> players = new ArrayList<>(20);
		for (int i = 0; i < 20; i++) {
			String playerName = randomized + "é" + format.format(i);
			fakePlayers.add(createPlayer(playerName, texts.containsKey(i) ? new ChatComponentText(texts.get(i)) : ChatComponentText.d, PING_ERROR));
			players.add(playerName);
		}
		createTeam("+00AAA" + randomized, players).forEach(teamPackets::add);
		
		players = new ArrayList<>(59);
		for (int i = 20; i < 80; i++) {
			String playerName = randomized + "é" + format.format(i);
			fakePlayers.add(createPlayer(playerName, i >= 60 && texts.containsKey(i - 40) ? new ChatComponentText(texts.get(i - 40)) : ChatComponentText.d, i >= 60 ? PING_ERROR : PING_FULL));
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
	
	private FakePlayer createPlayer(String playerName, IChatBaseComponent component, int ping) throws ReflectiveOperationException {
		GameProfile gameProfile = new GameProfile(UUID.randomUUID(), playerName);
		gameProfile.getProperties().put("textures", new Property("textures", "ewogICJ0aW1lc3RhbXAiIDogMTYyMTU0MjYxNTkwMSwKICAicHJvZmlsZUlkIiA6ICI5MWZlMTk2ODdjOTA0NjU2YWExZmMwNTk4NmRkM2ZlNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJoaGphYnJpcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mZmNjNTNlMTA2NzhhMGRlM2RjYjFiMDIwZTgyNjg1ZWI1M2FhNTU2YTdkMjMxMGQ5YjYwODIzYWVlYjUyYmIwIgogICAgfQogIH0KfQ==", "XIkfLAB7K4BO8wK685/X8HK5GFYejC/wNMpnjju8R/NK1nQl9nFCD7seLN5z6te26Q66NMxf5dsihgOp2eE6sjW+cfXvfZeGpNP6PI2iU3j4R+jWDcLcByL+ZdO6YAKqA0hIYdi3SapF7QHSbBamUxcZg9X9kLf7/tnL3AAzrYWP2hN6c+yQGo31RIXTEFScvdSahKDF1ifCTomjiFXUWD/3V6s9TiCZnhHGoUzSpjAs4Jr2SvjJk9BOHkSvRdch3vqvna6rs29kEMQSdXZxmYeuzeA5IW1iUFFN4/kG36yLg+01fON8V6HXs8fYVkZUzb5MQfgMTiLyUSpB8EyFred3pKu2qxQVfbAjr/lDz5NneIozYYvX8WxsgK+S+UfM5vZlMzkjWfrtUexaD5cvjZZw8DPPjsGjjpc+onPhk+pY+5b5hQ/4gEvSGNLd0an5J0txh8UZ1Jce0KWkJ7kQpCd0JGKUhy4q2vSpdOmpEP5+utliIivWDwoAHyJ2dO/5DFZ7yMgsETNBAxezxZTjG6qpwWQFpvx4lCz52WutC+ijJgYm9D8TIyXRgCqkcjZBpilK/PsfR9pbJlMN6LdYAh53TzZFT2IOerHEupj8dBhf51hM2ylXFODRozwcsmQ+thnhBOUGmXMJtNBTI/VjcF/jTTmYPztQmfKh4EmiRpw="));
		
		PacketPlayOutPlayerInfo packetInfoCreate = new PacketPlayOutPlayerInfo();
		Reflection.setFieldValue(packetInfoCreate, "a", EnumPlayerInfoAction.ADD_PLAYER);
		Reflection.setFieldValue(packetInfoCreate, "b", Arrays.asList(Reflection.<Object>instantiateNested(packetInfoCreate, "PlayerInfoData", gameProfile, ping, EnumGamemode.ADVENTURE, component)));
		
		PacketPlayOutPlayerInfo packetInfoRemove = new PacketPlayOutPlayerInfo();
		Reflection.setFieldValue(packetInfoRemove, "a", EnumPlayerInfoAction.REMOVE_PLAYER);
		Reflection.setFieldValue(packetInfoRemove, "b", Arrays.asList(Reflection.<Object>instantiateNested(packetInfoRemove, "PlayerInfoData", gameProfile, ping, EnumGamemode.ADVENTURE, component)));
		
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
