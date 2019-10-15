package fr.olympa.api.title;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Reflection;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Reflection.ClassEnum;

public class ActionBar {

	/**
	 * Permet de mettre du texte en haut de la hotbar (bar des objets)
	 *
	 * @param message String avec gestion des couleurs
	 */
	public static void sendActionBar(final Player player, String message) {

		message = SpigotUtils.color(message);

		final Class<?> packetPlayOutChat = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutChat");
		try {
			final Constructor<?> packetConstructor = packetPlayOutChat.getConstructor(Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent"), byte.class);
			final Class<?> ichat = Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent");
			final Class<?> chatSerializer = ichat.getClasses()[0];
			final Method csA = chatSerializer.getMethod("a", String.class);
			final Object component = csA.invoke(chatSerializer, "{\"text\":\"" + message + "\"}");
			final Object packet = packetConstructor.newInstance(component, (byte) 2);

			Reflection.sendPacket(player, packet);

		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
}
