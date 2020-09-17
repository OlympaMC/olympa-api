package fr.olympa.api.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.olympa.api.player.Gender;
import fr.olympa.api.server.OlympaServer;
import fr.olympa.api.utils.ColorUtils;
import fr.olympa.api.utils.Utils;

public enum OlympaGroup {

	FONDA(1, 100, OlympaServer.ALL, "Fondateur", "Fondatrice", "&4%s ", ":&c", true),
	ADMIN(2, 95, OlympaServer.ALL, "Administrateur", "Administatrice", "&4%s ", ":&r", true),
	MODP(6, 85, OlympaServer.ALL, "Modérateur+", "Modératrice+", "&c%s ", ":&r", true),
	RESP_TECH(5, 80, OlympaServer.ALL, "Resp._Technique", "Resp. Technique", "&3%s ", ":&r", true),
	MOD(7, 75, OlympaServer.ALL, "Modérateur", "Modératrice", "&6%s ", ":&r", false),
	ASSISTANT(8, 70, OlympaServer.ALL, "Assistant", "Assistante", "&e%s ", ":&r", false),
	RESP_STAFF(9, 65, OlympaServer.ALL, "Resp._Staff", "Resp. Staff", "&c%s ", ":&r", true),
	RESP_COM(5, 80, OlympaServer.ALL, "Resp._Communication", "Resp. Communication", "&3%s ", ":&r", true),
	RESP_ANIMATION(10, 60, OlympaServer.ALL, "Resp._Animateur", "Resp. Animateur", "&3%s ", ":&r", true),
	RESP_BUILDER(11, 55, OlympaServer.ALL, "Resp. Buildeur", "Resp. Buildeur", "&a%s ", ":&r", true),
	DEVP(19, 51, OlympaServer.ALL, "Développeur+", "Développeuse+", "&b%s ", ":&r", false),
	DEV(12, 50, OlympaServer.ALL, "Développeur", "Développeuse", "&b%s ", ":&r", false),
	BUILDER(14, 48, OlympaServer.ALL, "Buildeur", "Buildeuse", "&2%s ", ":&r", false),
	ANIMATEUR(13, 46, OlympaServer.ALL, "Animateur", "Animatrice", "&d%s ", ":&r", false),
	GRAPHISTE(15, 44, OlympaServer.ALL, "Graphiste", "Graphiste", "&b%s ", ":&r", false),
	FRIEND(16, 42, OlympaServer.ALL, "Ami", "Amie", "&e%s ", ":&r", false),
	YOUTUBER(17, 40, OlympaServer.ALL, "Youtubeur", "Youtubeuse", "&5%s ", ":&r", false),
	MINI_YOUTUBER(18, 38, OlympaServer.ALL, "M-Youtubeur", "M-Youtubeuse", "&d%s ", ":&r", false),
	PLAYER(20, 0, OlympaServer.ALL, "Joueur", "Joueuse", "&7", ":", false),
	MAFIEUX(21, 10, OlympaServer.ZTA, "Mafieux", "Mafieuse", "&d%s ", ":&r", false),

	CREA_CONSTRUCTOR(22, 1, OlympaServer.CREATIF, "Constructeur", "Constructrice", "&9%s ", ":&r", false),
	CREA_ARCHITECT(23, 2, OlympaServer.CREATIF, "Architecte", "Architecte", "&e%s ", ":&r", false),
	CREA_CREATOR(24, 3, OlympaServer.CREATIF, "Créateur", "Créatrice", "&6%s ", ":&r", false);
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
	final String chatSuffix;
	private boolean highStaff;

	public List<String> runtimePermissions = new ArrayList<>();

	private OlympaGroup(int id, int power, OlympaServer server, String name, String nameFem, String prefix, String chatSufix, boolean highStaff) {
		this.id = id;
		this.power = power;
		this.server = server;
		this.name = name;
		this.nameFem = nameFem;
		this.prefix = ColorUtils.color(prefix);
		chatSuffix = ColorUtils.color(chatSufix);
		this.highStaff = highStaff;
	}

	public Set<OlympaGroup> getAllGroups() {
		return Arrays.stream(OlympaGroup.values()).filter(group -> group.getPower() <= getPower()).collect(Collectors.toSet());
	}

	public String getChatColor() {
		int index = chatSuffix.length();
		return chatSuffix.substring(index - 3, index - 1);
	}

	public String getChatSuffix() {
		return chatSuffix;
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

	/*
	 * Get name without space & gender
	 */
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

	public boolean isHighStaff() {
		return highStaff;
	}

	/*
	 * getPrefix with %s
	 */
	@Deprecated
	public String getPrefix() {
		return prefix;
	}

	public String getPrefix(Gender gender) {
		return ColorUtils.color(String.join(prefix, getName(gender)));
	}

	public OlympaServer getServer() {
		return server;
	}
}
