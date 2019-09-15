package fr.tristiisch.olympa.api.objects;

import java.util.Arrays;

import fr.tristiisch.olympa.api.utils.SpigotUtils;
import fr.tristiisch.olympa.api.utils.Utils;

public enum OlympaGroup {

	FONDA(1, 100, OlympaServer.ALL, "Fondateur", "&4%rank ", ":&c"),
	ADMIN(2, 90, OlympaServer.ALL, "Admin", "&4%rank ", ":&r"),
	ASSISTANT(3, 80, OlympaServer.ALL, "Assistant", "&a%rank ", ":&r"),
	DEV(4, 60, OlympaServer.ALL, "Développeur", "&b%rank ", ":&r"),
	MODP(5, 55, OlympaServer.ALL, "Modérateur+", "&6%rank ", ":&r"),
	MOD(6, 50, OlympaServer.ALL, "Modérateur", "&6%rank ", ":&r"),
	BUILDER(7, 40, OlympaServer.ALL, "Builder", "&b%rank ", ":&r"),
	FRIEND(8, 30, OlympaServer.ALL, "Ami", "&a%rank ", ":&r"),

	MAFIEUX(9, 30, OlympaServer.GTA, "Mafieux", "&d%rank ", ":&r"),

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
