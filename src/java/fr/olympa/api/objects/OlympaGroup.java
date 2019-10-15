package fr.olympa.api.objects;

import java.util.Arrays;

import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;

public enum OlympaGroup {

	FONDA(1, 100, OlympaServer.ALL, "Fondateur", "&4%rank ", ":&c"),
	CO_FONDA(2, 95, OlympaServer.ALL, "Co-Fondateur", "&4%rank ", ":&c"),
	ADMIN(3, 90, OlympaServer.ALL, "Admin", "&4%rank ", ":&r"),
	DEV(4, 80, OlympaServer.ALL, "Développeur", "&b%rank ", ":&r"),
	MODP(5, 75, OlympaServer.ALL, "Modérateur+", "&6%rank ", ":&r"),
	MOD(6, 70, OlympaServer.ALL, "Modérateur", "&6%rank ", ":&r"),
	ASSISTANT(7, 60, OlympaServer.ALL, "Assistant", "&a%rank ", ":&r"),
	ANIMATEUR(8, 55, OlympaServer.ALL, "Animateur", "&a%rank ", ":&r"),
	BUILDER(8, 50, OlympaServer.ALL, "Builder", "&b%rank ", ":&r"),
	FRIEND(9, 40, OlympaServer.ALL, "Ami", "&a%rank ", ":&r"),
	YOUTUBER(10, 35, OlympaServer.ALL, "Youtuber", "&e%rank ", ":&r"),
	MINI_YOUTUBER(11, 30, OlympaServer.ALL, "Mini-Youtuber", "&e%rank ", ":&r"),

	MAFIEUX(12, 10, OlympaServer.GTA, "Mafieux", "&d%rank ", ":&r"),

	PLAYER(20, 0, OlympaServer.ALL, "Joueur", "&7", ":");

	/**
	 * Get {@link #OlympaGroup}
	 *
	 * @param id
	 * @return a OlympaGroup or null if id dosen't exist
	 */
	public static OlympaGroup getById(int id) {
		return Arrays.stream(OlympaGroup.values()).filter(group -> group.getId() == id).findFirst().orElse(null);
	}

	/**
	 * Get {@link #OlympaGroup}
	 *
	 * @param name (ignore case)
	 * @return a OlympaGroup or null if name dosen't exist
	 */
	public static OlympaGroup getByName(String name) {
		return Arrays.stream(OlympaGroup.values()).filter(group -> Utils.equalsIgnoreCase(group.getName(), name)).findFirst().orElse(null);
	}

	final int id;
	final int power;
	final OlympaServer server;
	final String name;
	final String prefix;
	final String chatSufix;

	private OlympaGroup(int id, int power, OlympaServer server, String name, String prefix, String chatSufix) {
		this.id = id;
		this.power = power;
		this.server = server;
		this.name = name;
		this.prefix = SpigotUtils.color(prefix.replaceFirst("%rank", this.name));
		this.chatSufix = SpigotUtils.color(chatSufix);
	}

	public String getChatSufix() {
		return this.chatSufix;
	}

	public int getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public int getPower() {
		return this.power;
	}

	public String getPrefix() {
		return this.prefix;
	}

	public OlympaServer getServer() {
		return this.server;
	}
}
