package fr.olympa.api.groups;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.player.Gender;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Utils;

public enum OlympaGroup {

	FONDA(1, 100, OlympaServer.ALL, "Fondateur", "Fondatrice", "&4%rank ", ":&c"),
	ADMIN(2, 95, OlympaServer.ALL, "Administrateur", "Administatrice", "&4%rank ", ":&r"),
	MODP(6, 85, OlympaServer.ALL, "Modérateur+", "Modératrice+", "&c%rank ", ":&r"),
	RESP_TECH(5, 80, OlympaServer.ALL, "Resp. Technique", "Resp. Technique", "&3%rank ", ":&r"),
	MOD(7, 75, OlympaServer.ALL, "Modérateur", "Modératrice", "&6%rank ", ":&r"),
	ASSISTANT(8, 70, OlympaServer.ALL, "Assistant", "Assistante", "&e%rank ", ":&r"),
	RESP_STAFF(9, 65, OlympaServer.ALL, "Resp. Staff", "Resp. Staff", "&c%rank ", ":&r"),
	RESP_ANIMATION(10, 60, OlympaServer.ALL, "Resp. Animateur", "Resp. Animateur", "&3%rank ", ":&r"),
	RESP_BUILDER(11, 55, OlympaServer.ALL, "Resp. Buildeur", "Resp. Buildeur", "&a%rank ", ":&r"),
	DEVP(19, 51, OlympaServer.ALL, "Développeur+", "Développeuse+", "&b%rank ", ":&r"),
	DEV(12, 50, OlympaServer.ALL, "Développeur", "Développeuse", "&b%rank ", ":&r"),
	BUILDER(14, 48, OlympaServer.ALL, "Buildeur", "Buildeuse", "&2%rank ", ":&r"),
	ANIMATEUR(13, 46, OlympaServer.ALL, "Animateur", "Animatrice", "&d%rank ", ":&r"),
	GRAPHISTE(15, 44, OlympaServer.ALL, "Graphiste", "Graphiste", "&b%rank ", ":&r"),
	FRIEND(16, 42, OlympaServer.ALL, "Ami", "Amie", "&e%rank ", ":&r"),
	YOUTUBER(17, 40, OlympaServer.ALL, "Youtubeur", "Youtubeuse", "&5%rank ", ":&r"),
	MINI_YOUTUBER(18, 38, OlympaServer.ALL, "M-Youtubeur", "M-Youtubeuse", "&d%rank ", ":&r"),
	PLAYER(20, 0, OlympaServer.ALL, "Joueur", "Joueuse", "&7", ":"),
	MAFIEUX(21, 10, OlympaServer.ZTA, "Mafieux", "Mafieuse", "&d%rank ", ":&r"),

	CREA_CONSTRUCTOR(22, 1, OlympaServer.CREATIF, "Constructeur", "Constructrice", "&9%rank ", ":&r"),
	CREA_ARCHITECT(23, 2, OlympaServer.CREATIF, "Architecte", "Architecte", "&e%rank ", ":&r"),
	CREA_CREATOR(24, 3, OlympaServer.CREATIF, "Créateur", "Créatrice", "&6%rank ", ":&r");
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
		this.prefix = ColorUtils.color(prefix);
		this.chatSufix = ColorUtils.color(chatSufix);
	}

	public Set<OlympaGroup> getAllGroups() {
		return Arrays.stream(OlympaGroup.values()).filter(group -> group.getPower() <= getPower()).collect(Collectors.toSet());
	}

	public String getChatColor() {
		int index = chatSufix.length();
		return chatSufix.substring(index - 3, index - 1);
	}

	public String getChatSufix() {
		return chatSufix;
	}

	public String getColor() {
		return prefix.substring(0, 2);
	}

	public int getId() {
		return id;
	}

	public int getIndex() {
		OlympaGroup[] groups = OlympaGroup.values();
		for (int i = 0; i < groups.length; i++)
			if (groups[i].getId() == id)
				return i + 1;
		return -1;
	}

	@Deprecated
	public String getName() {
		return name;
	}

	public String getName(Gender gender) {
		if (gender == Gender.FEMALE)
			return nameFem.replace("_", " ");
		return name.replace("_", " ");
	}

	public int getPower() {
		return power;
	}

	@Deprecated
	public String getPrefix() {
		return getPrefix(Gender.MALE);
	}

	public String getPrefix(Gender gender) {
		return ColorUtils.color(prefix.replace("%rank", getName(gender)));
	}

	public OlympaServer getServer() {
		return server;
	}
}
