package fr.olympa.api.common.bpmc;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.messaging.Messenger;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

/**
 * @Deprecated Don't use it, use Redis channels instead
 * https://www.spigotmc.org/wiki/bukkit-bungee-plugin-messaging-channel/ is unstable and need at least 1 player connected.
 */
@Deprecated
public class SpigotBPMCEvent implements PluginMessageListener {

	static Consumer<PlayerCountResponse> playerCountSuccess;
	static Consumer<PlayerIpResponse> playerIpSuccess;
	static Consumer<PlayerListResponse> playerListSuccess;
	static Consumer<String[]> getServersSuccess;
	static Consumer<String> getServerSuccess;

	public static Consumer<String[]> getGetServersSuccess() {
		return getServersSuccess;
	}

	public static Consumer<String> getGetServerSuccess() {
		return getServerSuccess;
	}

	public static Consumer<PlayerCountResponse> getPlayerCountSuccess() {
		return playerCountSuccess;
	}

	public static Consumer<PlayerIpResponse> getPlayerIpSuccess() {
		return playerIpSuccess;
	}

	public static Consumer<PlayerListResponse> getPlayerListSuccess() {
		return playerListSuccess;
	}

	public static void setGetServersSuccess(Consumer<String[]> getServersSuccess) {
		SpigotBPMCEvent.getServersSuccess = getServersSuccess;
	}

	public static void setGetServerSuccess(Consumer<String> getServerSuccess) {
		SpigotBPMCEvent.getServerSuccess = getServerSuccess;
	}

	public static void setPlayerCountSuccess(Consumer<PlayerCountResponse> playerCountSuccess) {
		SpigotBPMCEvent.playerCountSuccess = playerCountSuccess;
	}

	public static void setPlayerIpSuccess(Consumer<PlayerIpResponse> playerIpSuccess) {
		SpigotBPMCEvent.playerIpSuccess = playerIpSuccess;
	}

	public static void setPlayerListSuccess(Consumer<PlayerListResponse> playerListSuccess) {
		SpigotBPMCEvent.playerListSuccess = playerListSuccess;
	}

	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] message) {
		if (!channel.equals("BungeeCord"))
			return;
		ByteArrayDataInput in = ByteStreams.newDataInput(message);
		String subchannel = in.readUTF();
		if (subchannel.equals("PlayerCount")) {
			String server = in.readUTF();
			int playercount = in.readInt();
			if (playerCountSuccess != null)
				playerCountSuccess.accept(new PlayerCountResponse(server, playercount));
		} else if (subchannel.equals("PlayerList")) {
			String server = in.readUTF();
			String[] playerList = in.readUTF().split(", ");
			if (playerListSuccess != null)
				playerListSuccess.accept(new PlayerListResponse(server, playerList));
		} else if (subchannel.equals("GetServers")) {
			String[] serverList = in.readUTF().split(", ");
			if (getServersSuccess != null)
				getServersSuccess.accept(serverList);
		} else if (subchannel.equals("GetServer")) {
			String server = in.readUTF();
			if (getServerSuccess != null)
				getServerSuccess.accept(server);
		} else if (subchannel.equals("IP")) {
			String ip = in.readUTF();
			int port = in.readInt();
			if (playerIpSuccess != null)
				playerIpSuccess.accept(new PlayerIpResponse(ip, port));
		}
	}

	public void register(Plugin plugin) {
		Messenger messenger = plugin.getServer().getMessenger();
		messenger.registerIncomingPluginChannel(plugin, "BungeeCord", this);
	}
}
