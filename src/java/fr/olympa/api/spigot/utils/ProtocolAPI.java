package fr.olympa.api.spigot.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.common.chat.ColorUtils;
import net.md_5.bungee.protocol.ProtocolConstants;

/**
 * Lors d'une nouvelle versionn de protocol, récupérer le numéro via ce lien: https://wiki.vg/Protocol_version_numbers
 * - Ajouter dans l'eum
 * - Mettre à jour le repo maven de protocolLib
 * - Mettre a jour le .jar ProtocolLib
 * - Mettre a jour le .jar Cs Stats sur le bungee
 * - Ajouter ViaVersion si la version serveur sur olympa n'est pas la dernière disponible
 */
public enum ProtocolAPI {

	V1_17(755, false, true),
	V1_16_5(754),
	V1_16_4(754),
	V1_16_3(753),
	V1_16_2(751),
	V1_16_1(736),
	V1_16(735),
	V1_15_2(578),
	V1_15_1(575),
	V1_15(573),
	V1_14_4(498),
	V1_14_3(490),
	V1_14_2(485),
	V1_14_1(480),
	V1_14(477),
	V1_13_2(404),
	V1_13_1(401),
	V1_13(393),
	V1_12_2(340),
	V1_12_1(338),
	V1_12(335),
	V1_11_2(316),
	V1_11_1(316),
	V1_11(315),
	V1_10_2(210),
	V1_10_1(210),
	V1_10(210),
	V1_9_4(110),
	V1_9_3(110),
	V1_9_2(109),
	V1_9_1(108),
	V1_9(107),
	V1_8_9(47, true, true),
	V1_8_8(47, true, true),
	V1_8_7(47, true, true),
	V1_8_6(47, true, true),
	V1_8_5(47, true, true),
	V1_8_4(47, true, true),
	V1_8_3(47, true, true),
	V1_8_2(47, true, true),
	V1_8_1(47, true, true),
	V1_8(47, true, true),
	V1_7_10(5, false),
	V1_7_9(5, false),
	V1_7_8(5, false),
	V1_7_7(5, false),
	V1_7_6(5, false),
	V1_7_5(4, false),
	V1_7_4(4, false),
	V1_7_2(4, false),
	V1_7(4, false),
	V1_6_4(78, false),
	V1_6_2(74, false),
	V1_6_1(73, false),
	V1_5_2(61, false),
	V1_5_1(60, false),
	V1_4_7(51, false),
	;

	private static String version = null;
	private static ProtocolAPI defaultProtocol = null;

	/**
	 * Get the big version of protocol @param protocolNumber
	 * Prefer using {@link #getAll(int)} to get all ProtocolAPI with a protocolNumber as they are severial ProtocolAPI with the same protocolId
	 */
	@Nullable
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI get(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).findFirst().orElse(null);
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static List<ProtocolAPI> getAll(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).collect(Collectors.toList());
	}

	@Nullable
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI get(String version) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getName().equals(version)).findFirst().orElse(null);
	}

	/**
	 * Get default Spigot protocol
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static ProtocolAPI getDefaultSpigotProtocol() {
		if (defaultProtocol == null)
			defaultProtocol = get(getSpigotVersion());
		return defaultProtocol;
	}

	/**
	 * Get big version allowed protocols for Olympa Servers, based on {@link #allow} & {@link #notRecommended}.
	 * Bad use for a server like ZTA, as this server only allows one version and the method is not affected by this change.
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI getLastVersion() {
		return Arrays.stream(ProtocolAPI.values()).filter(protocolApi -> protocolApi.isAllowed() && !protocolApi.isNotRecommended()).findFirst().orElse(null);
	}

	/**
	 * Get lowest version allowed protocols for Olympa Servers, based on {@link #allow} & {@link #notRecommended}.
	 * Bad use for a server like ZTA, as this server only allows one version and the method is not affected by this change.
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI getFirstVersion() {
		//		return Arrays.stream(ProtocolAPI.values()).filter(protocolApi -> protocolApi.isAllowed() && !protocolApi.isNotRecommended()).reduce((first, second) -> second).orElse(null);
		ProtocolAPI[] array = ProtocolAPI.values();
		int index = array.length;
		while (--index >= 0) {
			ProtocolAPI protocolApi = array[index];
			if (protocolApi.isAllowed() && !protocolApi.isNotRecommended())
				return protocolApi;
		}
		return null;
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static String getSpigotVersion() {
		if (!LinkSpigotBungee.Provider.link.isSpigot())
			throw new IllegalAccessError("Can't get BukkitVersion on Bungee instance.");
		if (version == null)
			version = Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf('-'));
		return version;
	}

	/**
	 * Return all versions names natively supported by Bungeecord.
	 * It dosen't contains other versions supported with ProtocolSupportBungee or ViaVersionBungee
	 * @throw IllegalAccessError when spigot instance call this method
	 */
	@Nullable
	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public static List<String> getBungeeVersion() {
		if (LinkSpigotBungee.Provider.link.isSpigot())
			throw new IllegalAccessError("Can't get BungeeVersion on Spigot instance.");
		return ProtocolConstants.SUPPORTED_VERSIONS;
	}

	/**
	 * Return all versions id natively supported by Bungeecord.
	 * It dosen't contains other versions supported with ProtocolSupportBungee or ViaVersionBungee
	 * @throw IllegalAccessError when spigot instance call this method
	 */
	@Nullable
	@SpigotOrBungee(allow = AllowedFramework.BUNGEE)
	public static List<Integer> getBungeeVersionId() {
		if (LinkSpigotBungee.Provider.link.isSpigot())
			throw new IllegalAccessError("Can't get BungeeVersionId on Spigot instance.");
		return ProtocolConstants.SUPPORTED_VERSION_IDS;
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static String getVersionSupportedToString() {
		List<String> vers = getVersionSupported();
		return vers.isEmpty() ? null : ColorUtils.join(vers);
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static String[] getVersionSupportedArray() {
		List<String> vers = getVersionSupported();
		return new String[] { vers.get(vers.size() - 1), vers.get(0) };
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static List<String> getVersionSupported() {
		ProtocolAPI defaultProto = getDefaultSpigotProtocol();
		if (defaultProto != null)
			return getAll(defaultProto.getProtocolNumber()).stream().map(ProtocolAPI::getName).collect(Collectors.toList());
		return new ArrayList<>();
	}

	private final int protocolNumber;
	private boolean allow = true;
	private boolean notRecommended = false;

	ProtocolAPI(int protocolNumber) {
		this.protocolNumber = protocolNumber;
	}

	ProtocolAPI(int protocolNumber, boolean allow) {
		this(protocolNumber);
		this.allow = allow;
	}

	ProtocolAPI(int protocolNumber, boolean allow, boolean notRecommanded) {
		this(protocolNumber, allow);
		notRecommended = notRecommanded;
	}

	public String getName() {
		return name().replace("_", ".").substring(1);
	}

	public int getProtocolNumber() {
		return protocolNumber;
	}

	public boolean isAllowed() {
		return allow;
	}

	public boolean isNotRecommended() {
		return notRecommended;
	}

	public boolean isSupported() {
		ProtocolAPI defaultProto = getDefaultSpigotProtocol();
		return defaultProto != null && getProtocolNumber() <= defaultProto.getProtocolNumber();
	}
}
