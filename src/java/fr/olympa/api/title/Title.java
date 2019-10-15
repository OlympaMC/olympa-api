package fr.olympa.api.title;

import java.lang.reflect.Constructor;

import org.bukkit.entity.Player;

import fr.olympa.api.utils.Reflection;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Reflection.ClassEnum;

public class Title {

	public static void clearTitle(final Player player) {
		sendTitle(player, "", "", 0, 0, 0);
	}

	/**
	 * Permet de mettre du texte en plein milieu de l\"écran (fadeIn = 20, stay = 40, fadeOut = 20)
	 *
	 * @param title String avec gestion des couleurs
	 * @param subtitle String avec gestion des couleurs
	 */
	public static void sendTitle(final Player player, final String title, final String subtitle) {
		sendTitle(player, title, subtitle, 20, 40, 20);
	}

	/**
	 * Permet de mettre du texte en plein milieu de l\"écran
	 *
	 * @param fadeIn Integer Temps de l\"animation fondu quand le texte apparait (0 = pas d\"animation)
	 * @param stay Integer Temps de l\"affichage du texte (sans compter les animations)
	 * @param fadeOut Integer Temps de l\"animation fondu quand le texte disapparait (0 = pas d\"animation)
	 * @param title String avec gestion des couleurs
	 * @param subtitle String avec gestion des couleurs
	 */
	public static void sendTitle(final Player player, final String title, final String subtitle, final Integer fadeIn, final Integer stay, final Integer fadeOut) {
		try {

			final Object enumTitle = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutTitle").getDeclaredClasses()[0].getField("TITLE").get(null);
			final Object titleChat = Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + SpigotUtils.color(title) + "\"}");

			final Object enumSubtitle = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutTitle").getDeclaredClasses()[0].getField("SUBTITLE").get(null);
			final Object subtitleChat = Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent").getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + SpigotUtils.color(subtitle) + "\"}");

			final Constructor<?> titleConstructor = Reflection.getClass(ClassEnum.NMS, "PacketPlayOutTitle")
					.getConstructor(Reflection.getClass(ClassEnum.NMS, "PacketPlayOutTitle").getDeclaredClasses()[0],
							Reflection.getClass(ClassEnum.NMS, "IChatBaseComponent"),
							int.class,
							int.class,
							int.class);
			final Object titlePacket = titleConstructor.newInstance(enumTitle, titleChat, fadeIn, stay, fadeOut);
			final Object subtitlePacket = titleConstructor.newInstance(enumSubtitle, subtitleChat, fadeIn, stay, fadeOut);

			Reflection.sendPacket(player, titlePacket);
			Reflection.sendPacket(player, subtitlePacket);

		} catch (final Exception e1) {
			e1.printStackTrace();
		}
	}
}
