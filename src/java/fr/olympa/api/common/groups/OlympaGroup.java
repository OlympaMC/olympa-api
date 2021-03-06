package fr.olympa.api.common.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.player.Gender;
import fr.olympa.api.common.server.OlympaServer;
import fr.olympa.api.utils.Utils;

public enum OlympaGroup implements Comparable<OlympaGroup> {

	FONDA(1, 100, OlympaServer.ALL, "Fondateur", "Fondatrice", "&4%s ", ":&c", true),
	ADMIN(2, 95, OlympaServer.ALL, "Administrateur", "Administratrice", "&4Admin", "&4%s ", ":&r", true),
	AMBASSADOR(27, 33, OlympaServer.ALL, "Ambassadeur", "Ambassadrice", "&cAmbassadeur ", "&c%s ", ":&r", false),
	RESP(28, 90, OlympaServer.ALL, "Responsable", "Responsable", "&c%s ", ":&r", true, false),
	RESP_TECH(3, 80, OlympaServer.ALL, "Resp_Technique", "Resp_Technique", "&3%s ", ":&r", true),
	RESP_STAFF(9, 75, OlympaServer.ALL, "Resp_Staff", "Resp_Staff", "&c%s ", ":&r", true),
	RESP_COM(5, 70, OlympaServer.ALL, "Resp_Communication", "Resp_Communication", "&3%s ", ":&r", true),
	RESP_ANIMATION(10, 65, OlympaServer.ALL, "Resp_Animateur", "Resp_Animatrice", "&5%s ", ":&r", true),
	RESP_BUILDER(11, 60, OlympaServer.ALL, "Resp_Buildeur", "Resp_Buildeuse", "&2%s ", ":&r", true),
	RESP_GAMES(35, 57, OlympaServer.ALL, "Resp_Game_Design", "Resp_Game_Design", "&6%s ", ":&r", true),
	MODP(6, 55, OlympaServer.ALL, "Modérateur+", "Modératrice+", "&c%s ", ":&r", true),
	MOD(7, 53, OlympaServer.ALL, "Modérateur", "Modératrice", "&6%s ", ":&r", false),
	ASSISTANT(8, 50, OlympaServer.ALL, "Assistant", "Assistante", "&e%s ", ":&r", false),
	DEVP(26, 45, OlympaServer.ALL, "Développeur+", "Développeuse+", "&b%s ", ":&r", false),
	DEV(12, 43, OlympaServer.ALL, "Développeur", "Développeuse", "&b%s ", ":&r", false),
	BUILDER(14, 40, OlympaServer.ALL, "Buildeur", "Buildeuse", "&a%s ", ":&r", false),
	GAMEDESIGNER(4, 39, OlympaServer.ALL, "GameDesigner", "GameDesigner", "&6%s ", ":&r", true),
	ANIMATOR(13, 38, OlympaServer.ALL, "Animateur", "Animatrice", "&d%s ", ":&r", false),
	GRAPHISTE(15, 36, OlympaServer.ALL, "Graphiste", "Graphiste", "&a%s ", ":&r", false),
	PARTENER(21, 35, OlympaServer.LOBBY, "Partenaire", "Partenaire", "&d%s ", ":&r", false),
	YOUTUBER(17, 32, OlympaServer.ALL, "Youtubeur", "Youtubeuse", "&5%s ", ":&r", false),
	MINI_YOUTUBER(18, 30, OlympaServer.ALL, "M-Youtubeur", "M-Youtubeuse", "&d%s ", ":&r", false),

	VIP(
			19,
			26,
			OlympaServer.LOBBY,
			"VIP",
			"VIP",
			"&6%s ",
			":&r",
			false),

	ZTA_LEGENDE(34, 25, OlympaServer.ZTA, "Légende", "Légende", "&3%s ", ":&r", false),
	ZTA_HEROS(33, 24, OlympaServer.ZTA, "Héros", "Héroïne", "&3%s ", ":&r", false),
	ZTA_SAUVEUR(32, 23, OlympaServer.ZTA, "Sauveur", "Sauveuse", "&3%s ", ":&r", false),
	ZTA_RODEUR(31, 22, OlympaServer.ZTA, "Rôdeur", "Rodeuse", "&6%s ", ":&r", false),
	ZTA_SURVIVANT(30, 21, OlympaServer.ZTA, "Survivant", "Survivante", "&e%s ", ":&r", false),

	CREA_CREATOR(24, 23, OlympaServer.CREATIF, "Créateur", "Créatrice", "&3%s ", ":&r", false),
	CREA_ARCHITECT(23, 22, OlympaServer.CREATIF, "Architecte", "Architecte", "&6%s ", ":&r", false),
	CREA_CONSTRUCTOR(29, 21, OlympaServer.CREATIF, "Bâtisseur", "Bâtisseuse", "&e%s ", ":&r", false),

	PVPKIT_CHAMPION(25, 28, OlympaServer.PVPKIT, "Champion", "Championne", "&6%s ", ":&r", false),

	FRIEND(16, 1, OlympaServer.ALL, "Ami", "Amie", "&7", ":", false),
	PLAYER(20, 0, OlympaServer.ALL, "Joueur", "Joueuse", "&7", ":", false);

	static {
		Map<Integer, List<OlympaGroup>> duplicateGroups = new HashMap<>();
		Arrays.stream(values()).forEach(group -> {
			if (group.getLegacyNameAndPrefix() == null || group.getLegacyNameAndPrefix().isBlank())
				Arrays.stream(Gender.values()).forEach(gender -> {
					String prefix = group.getPrefix(gender);
					if (prefix.length() > 16)
						LinkSpigotBungee.getInstance().sendMessage("&4[SMALL ERROR] &cGroup %s should have legacy prefix and name setup cause 1.8 to 1.13 will see gender %s like '%s'",
								group.getName(), gender, prefix.substring(0, 16));
				});

			List<OlympaGroup> list = duplicateGroups.get(group.getId());
			if (list == null) {
				list = new ArrayList<>();
				duplicateGroups.put(group.getId(), list);
			}
			list.add(group);
		});
		duplicateGroups.forEach((id, list) -> {
			if (list.size() != 1)
				LinkSpigotBungee.getInstance().sendMessage("&4[SEVERE ERROR] &cThere are several groups with the same id : %d %s", id, list.stream().map(OlympaGroup::getName).collect(Collectors.joining(", ")));
		});
	}

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
	private final String legacyNameAndPrefix;
	private final String chatSuffix;
	private final boolean highStaff;
	private boolean visible = true;

	public Map<String, Boolean> runtimePermissions = new HashMap<>();

	OlympaGroup(int id, int power, OlympaServer server, String name, String nameFem, String legacyNameAndPrefix, String prefix, String chatSuffix, boolean highStaff) {
		this.id = id;
		this.power = power;
		this.server = server;
		this.name = name;
		this.legacyNameAndPrefix = legacyNameAndPrefix;
		this.nameFem = nameFem;
		this.prefix = ColorUtils.color(prefix);
		this.chatSuffix = ColorUtils.color(chatSuffix);
		this.highStaff = highStaff;
	}

	OlympaGroup(int id, int power, OlympaServer server, String name, String nameFem, String prefix, String chatSuffix, boolean highStaff) {
		this(id, power, server, name, nameFem, null, prefix, chatSuffix, highStaff);
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

	@Nullable
	public String getLegacyNameAndPrefix() {
		return legacyNameAndPrefix;
	}

}
