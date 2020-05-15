package fr.olympa.api.scoreboard.sign;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import fr.olympa.api.utils.Reflection;
import fr.olympa.api.utils.spigot.ProtocolAPI;
import net.minecraft.server.v1_15_R1.ChatComponentText;
import net.minecraft.server.v1_15_R1.PacketPlayOutScoreboardTeam;

public class VirtualTeam {
	private final String name;
	private String prefix;
	private String suffix;
	private String currentPlayer;
	private String oldPlayer;
	private String cachedValue;

	private boolean prefixChanged, suffixChanged, playerChanged = false;
	private boolean first = true;

	protected VirtualTeam(String name) {
		this(name, "", "");
	}

	private VirtualTeam(String name, String prefix, String suffix) {
		this.name = name;
		this.prefix = prefix;
		this.suffix = suffix;
		cachedValue = "";
	}

	@SuppressWarnings("unchecked")
	public Object addOrRemovePlayer(int mode, String playerName) {
		Object packet = new PacketPlayOutScoreboardTeam();
		Reflection.setField(packet, "a", name);
		Reflection.setField(packet, "i", mode);

		try {
			Field f = packet.getClass().getDeclaredField("h");
			f.setAccessible(true);
			((List<String>) f.get(packet)).add(playerName);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}

		return packet;
	}

	private Object createPacket(int mode) {
		Object packet = new PacketPlayOutScoreboardTeam();
		Reflection.setField(packet, "a", name);
		Reflection.setField(packet, "i", mode);
		Reflection.setField(packet, "b", new ChatComponentText(""));
		Reflection.setField(packet, "c", new ChatComponentText(prefix));
		Reflection.setField(packet, "d", new ChatComponentText(suffix));
		Reflection.setField(packet, "j", 0);
		Reflection.setField(packet, "e", "always");

		return packet;
	}

	public Object createTeam() {
		return createPacket(0);
	}

	public String getCurrentPlayer() {
		return currentPlayer;
	}

	public String getName() {
		return name;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getValue() {
		return cachedValue;
	}

	public Object removeTeam() {
		Object packet = new PacketPlayOutScoreboardTeam();
		Reflection.setField(packet, "a", name);
		Reflection.setField(packet, "i", 1);
		first = true;
		return packet;
	}

	public void reset() {
		prefixChanged = false;
		suffixChanged = false;
		playerChanged = false;
		oldPlayer = null;
	}

	public Iterable<Object> sendLine() {
		List<Object> packets = new ArrayList<>();

		if (first) {
			packets.add(createTeam());
		} else if (prefixChanged || suffixChanged) {
			packets.add(updateTeam());
		}

		if (first || playerChanged) {
			if (oldPlayer != null) {
				packets.add(addOrRemovePlayer(4, oldPlayer));
			}
			packets.add(addOrRemovePlayer(3, currentPlayer));
		}

		if (first) {
			first = false;
		}

		return packets;
	}

	private void setPlayer(String name) {
		if (currentPlayer == null || !currentPlayer.equals(name)) {
			playerChanged = true;
		}
		oldPlayer = currentPlayer;
		currentPlayer = name;
	}

	private void setPrefix(String prefix) {
		if (this.prefix == null || !this.prefix.equals(prefix)) {
			prefixChanged = true;
		}
		this.prefix = prefix;
	}

	private void setSuffix(String suffix) {
		if (this.suffix == null || !this.suffix.equals(prefix)) {
			suffixChanged = true;
		}
		this.suffix = suffix;
	}

	public void setValue(String value) {
		if (value.length() <= 16) {
			setPrefix("");
			setSuffix("");
			setPlayer(value);
		} else if (value.length() <= 32) {
			setPrefix(value.substring(0, 16));
			setPlayer(value.substring(16));
			setSuffix("");
		} else if (value.length() <= 48) {
			setPrefix(value.substring(0, 16));
			setPlayer(value.substring(16, 32));
			setSuffix(value.substring(32));
		} else if (ProtocolAPI.V1_13.isSupported()) {
			setPrefix(value.substring(0, 16));
			setPlayer(value.substring(16, 32));
			setSuffix(value.substring(32));
		} else {
			throw new IllegalArgumentException("Too long value for < 1.13 ! Max 48 characters, value was " + value.length() + " !");
		}
		cachedValue = value;
	}

	public Object updateTeam() {
		return createPacket(2);
	}
}
