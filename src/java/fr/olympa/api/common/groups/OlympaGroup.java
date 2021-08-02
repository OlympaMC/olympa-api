package fr.olympa.api.common.groups;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.utils.Utils;

public enum OlympaGroup implements Comparable<OlympaGroup> {

	FONDA(1, 100, OlympaServer.ALL, "Fondateur", "Fondatrice", "&4%s ", ":&c", true),
	ADMIN(2, 95, OlympaServer.ALL, "Administrateur", "Administatrice", "&4%s ", ":&r", true),
	RESP(25, 90, OlympaServer.ALL, "Responsable", "Responsable", "&c%s ", ":&r", true, false),
	RESP_TECH(3, 80, OlympaServer.ALL, "Resp._Technique", "Resp._Technique", "&3%s ", ":&r", true),
	RESP_STAFF(9, 75, OlympaServer.ALL, "Resp._Staff", "Resp._Staff", "&c%s ", ":&r", true),
	RESP_COM(5, 70, OlympaServer.ALL, "Resp._Communication", "Resp._Communication", "&3%s ", ":&r", true),
	RESP_ANIMATION(10, 65, OlympaServer.ALL, "Resp._Animateur", "Resp._Animatrice", "&5%s ", ":&r", true),
	RESP_BUILDER(11, 60, OlympaServer.ALL, "Resp._Buildeur", "Resp._Buildeuse", "&2%s ", ":&r", true),
	MODP(6, 55, OlympaServer.ALL, "Modérateur+", "Modératrice+", "&c%s ", ":&r", true),
	MOD(7, 53, OlympaServer.ALL, "Modérateur", "Modératrice", "&6%s ", ":&r", false),
	ASSISTANT(8, 50, OlympaServer.ALL, "Assistant", "Assistante", "&e%s ", ":&r", false),
	DEVP(26, 45, OlympaServer.ALL, "Développeur+", "Développeuse+", "&b%s ", ":&r", false),
	DEV(12, 43, OlympaServer.ALL, "Développeur", "Développeuse", "&b%s ", ":&r", false),
	BUILDER(14, 40, OlympaServer.ALL, "Buildeur", "Buildeuse", "&a%s ", ":&r", false),
	GAMEMASTER(4, 39, OlympaServer.ALL, "GameMaster", "GameMaster", "&0%s ", ":&r", true),
	ANIMATOR(13, 38, OlympaServer.ALL, "Animateur", "Animatrice", "&d%s ", ":&r", false),
	GRAPHISTE(15, 36, OlympaServer.ALL, "Graphiste", "Graphiste", "&a%s ", ":&r", false),
	YOUTUBER(17, 32, OlympaServer.ALL, "Youtubeur", "Youtubeuse", "&5%s ", ":&r", false),
	MINI_YOUTUBER(18, 30, OlympaServer.ALL, "M-Youtubeur", "M-Youtubeuse", "&d%s ", ":&r", false),

	VIPPLUS(
			25,
			21,
			OlympaServer.ALL,
			"VIP+",
			"VIP+",
			"&6%s ",
			":&r",
			false),
	VIP(
			19,
			20,
			OlympaServer.ALL,
			"VIP",
			"VIP",
			"&6%s ",
			":&r",
			false),

	CREA_CREATOR(24, 23, OlympaServer.CREATIF, "Créateur", "Créatrice", "&3%s ", ":&r", false),
	CREA_ARCHITECT(23, 22, OlympaServer.CREATIF, "Architecte", "Architecte", "&6%s ", ":&r", false),
	CREA_CONSTRUCTOR(25, 21, OlympaServer.CREATIF, "Bâtisseur", "Bâtisseuse", "&e%s ", ":&r", false),

	FRIEND(16, 0, OlympaServer.ALL, "Ami", "Amie", "&7", ":", false),
	PLAYER(20, 0, OlympaServer.ALL, "Joueur", "Joueuse", "&7", ":", false);

	//ZTA_MAFIEUX(21, 10, OlympaServer.ZTA, "Mafieux", "Mafieuse", "&d%s ", ":&r", false),

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

	public static List<OlympaGroup> getStaffGroups() {
		return Arrays.stream(OlympaGroup.values()).filter(group -> group.getPower() >= GRAPHISTE.getPower()).collect(Collectors.toList());
	}

	private final int id;
	private final int power;
	private final OlympaServer server;
	private final String name;
	private final String nameFem;
	private final String prefix;
	private final String chatSuffix;
	private final boolean highStaff;
	private boolean visible = true;

	public Map<String, Boolean> runtimePermissions = new HashMap<>();

	OlympaGroup(int id, int power, OlympaServer server, String name, String nameFem, String prefix, String chatSuffix, boolean highStaff) {
		this.id = id;
		this.power = power;
		this.server = server;
		this.name = name;
		this.nameFem = nameFem;
		this.prefix = ColorUtils.color(prefix);
		this.chatSuffix = ColorUtils.color(chatSuffix);
		this.highStaff = highStaff;
	}

	OlympaGroup(int id, int power, OlympaServer server, String name, String nameFem, String prefix, String chatSuffix, boolean highStaff, boolean visible) {
		this(id, power, server, name, nameFem, prefix, chatSuffix, highStaff);
		this.visible = visible;
	}

	public Stream<OlympaGroup> getAllGroups() {
		return Arrays.stream(OlympaGroup.values()).filter(group -> group.getPower() <= getPower());
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

	public boolean isVisible() {
		return visible;
	}

	public int getIndex() {
		return ordinal();
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
		return ColorUtils.color(String.format(prefix, getName(gender)));
	}

	public OlympaServer getServer() {
		return server;
	}

	public void setRuntimePermission(String permission) {
		runtimePermissions.put(permission, true);
	}

	public void setRuntimePermission(String permission, boolean value) {
		runtimePermissions.put(permission, value);
	}

	public void unsetRuntimePermission(String permission) {
		runtimePermissions.remove(permission);
	}

}
