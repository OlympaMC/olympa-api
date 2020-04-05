package fr.olympa.api.groups;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.objects.OlympaServer;
import fr.olympa.api.utils.SpigotUtils;
import fr.olympa.api.utils.Utils;

public enum OlympaGroup {

	FONDA(1, 100, OlympaServer.ALL, "Fondateur", "Fondatrice", "&4%rank ", ":&c"),
	ADMIN(2, 95, OlympaServer.ALL, "Admin", "Admine", "&4%rank ", ":&r"),
	MODP(6, 85, OlympaServer.ALL, "Modérateur+", "Modératrice+", "&c%rank ", ":&r"),
	RESP_TECH(5, 80, OlympaServer.ALL, "RespTech", "RespTech", "&3%rank ", ":&r"),
	MOD(7, 75, OlympaServer.ALL, "Modérateur", "Modératrice", "&c%rank ", ":&r"),
	ASSISTANT(8, 70, OlympaServer.ALL, "Assistant", "Assistante", "&6%rank ", ":&r"),
	RESP_STAFF(9, 65, OlympaServer.ALL, "RespStaff", "RespStaff", "&c%rank ", ":&r"),
	RESP_ANIM(10, 60, OlympaServer.ALL, "RespAnim", "RespAnim", "&3%rank ", ":&r"),
	RESP_BUILDER(11, 55, OlympaServer.ALL, "RespBuildeur", "RespBuildeur", "&a%rank ", ":&r"),
	DEV(12, 50, OlympaServer.ALL, "Développeur", "Développeuse", "&b%rank ", ":&r"),
	ANIMATEUR(13, 48, OlympaServer.ALL, "Animateur", "Animatrice", "&d%rank ", ":&r"),
	BUILDER(14, 46, OlympaServer.ALL, "Buildeur", "Buildeuse", "&2%rank ", ":&r"),
	GRAPHISTE(15, 44, OlympaServer.ALL, "Graphiste", "Graphiste", "&3%rank ", ":&r"),
	FRIEND(16, 42, OlympaServer.ALL, "Ami", "Amie", "&e%rank ", ":&r"),
	YOUTUBER(17, 40, OlympaServer.ALL, "Youtubeur", "Youtubeuse", "&e%rank ", ":&r"),
	MINI_YOUTUBER(18, 38, OlympaServer.ALL, "M-Youtubeur", "M-Youtubeuse", "&e%rank ", ":&r"),
	PLAYER(20, 0, OlympaServer.ALL, "Joueur", "Joueuse", "&7", ":"),
	MAFIEUX(21, 10, OlympaServer.ZTA, "Mafieux", "Mafieuse", "&d%rank ", ":&r");

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
	final String nameFem;
	final String prefix;
	final String chatSufix;

	private OlympaGroup(int id, int power, OlympaServer server, String name, String nameFem, String prefix, String chatSufix) {
		this.id = id;
		this.power = power;
		this.server = server;
		this.name = name;
		this.nameFem = nameFem;
		this.prefix = SpigotUtils.color(prefix.replaceFirst("%rank", this.name));
		this.chatSufix = SpigotUtils.color(chatSufix);
	}

	public Set<OlympaGroup> getAllGroups() {
		return Arrays.stream(OlympaGroup.values()).filter(group -> group.getPower() <= getPower()).collect(Collectors.toSet());
	}

	public String getChatSufix() {
		return chatSufix;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public int getPower() {
		return power;
	}

	public String getPrefix() {
		return prefix;
	}

	public OlympaServer getServer() {
		return server;
	}
}
