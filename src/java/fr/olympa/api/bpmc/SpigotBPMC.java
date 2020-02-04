package fr.olympa.api.bpmc;

import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.ChannelNotRegisteredException;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import fr.olympa.api.groups.OlympaGroup;
import fr.olympa.api.objects.OlympaPlayer;
import fr.olympa.api.objects.OlympaServer;
import fr.olympa.core.spigot.OlympaCore;

/*
 * pas fini @Tristiisch
 */
public class SpigotBPMC {

	/*
	 * Get player count of the server name, or ALL to get the global player count
	 */
	public static void getPlayerCount(Player player, String serverName, Consumer<PlayerCountResponse> success) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("PlayerCount");
		out.writeUTF(serverName);
		player.sendPluginMessage(OlympaCore.getInstance(), "BungeeCord", out.toByteArray());
		SpigotBPMCEvent.setPlayerCountSuccess(success);
	}

	public static void sendNewGroup(Player player, OlympaPlayer olympaPlayer, OlympaGroup newGroup) {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("NewGroup");
		out.writeUTF(player.getUniqueId().toString());
		out.writeUTF(String.valueOf(newGroup.getId()));

		player.sendPluginMessage(OlympaCore.getInstance(), "Olympa", out.toByteArray());
	}

	/*
	 * Send player to a type server; bungeecord will define the good choice between for exemple lobby1 and lobby2
	 */
	public static void sendPlayer(Player player, OlympaServer server) {
		if (server == OlympaServer.ALL) {
			try {
				new Exception("Can't send " + player.getName() + " to server ALL");
			} catch (Exception e) {
				e.printStackTrace();
			}
			return;
		}
		String serverName = server.toString().toLowerCase();
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(serverName);
		player.sendPluginMessage(OlympaCore.getInstance(), "Olympa", out.toByteArray());
	}

	/*
	 * Send player to a server name like in bungeecord config
	 */
	public static void sendPlayer(Player player, String serverName) throws ChannelNotRegisteredException {
		ByteArrayDataOutput out = ByteStreams.newDataOutput();
		out.writeUTF("Connect");
		out.writeUTF(serverName);
		player.sendPluginMessage(OlympaCore.getInstance(), "BungeeCord", out.toByteArray());
	}
}
