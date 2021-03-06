package fr.olympa.api.spigot.utils;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.bukkit.Bukkit;

import fr.olympa.api.LinkSpigotBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee;
import fr.olympa.api.common.annotation.SpigotOrBungee.AllowedFramework;
import fr.olympa.api.common.chat.ColorUtils;
import fr.olympa.api.common.sort.Sorting;
import fr.olympa.api.utils.Utils;

/**
 * Lors d'une nouvelle versionn de protocol, récupérer le numéro via ce lien: https://wiki.vg/Protocol_version_numbers
 * - Ajouter dans l'eum
 * - Mettre à jour le repo maven de protocolLib (pour les dépandances du core)
 * - Mettre a jour le .jar ProtocolLib https://www.spigotmc.org/resources/protocolsupport.7201/
 * - Mettre a jour le .jar ClientStats sur le bungee https://www.spigotmc.org/resources/clientstats-bungee.27919/
 * - Ajouter ViaVersion si la version serveur sur olympa n'est pas la dernière disponible https://www.spigotmc.org/resources/viaversion.19254
 */
public enum ProtocolAPI {

	UNKNOW(999999, false, true),
	V1_17_1(756, true, true),
	V1_17(755, true, true),
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
	private static ProtocolAPI defaultVersion = null;

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static String getRange(List<ProtocolAPI> protocols) {
		if (protocols == null || protocols.isEmpty())
			return "unknown";
		List<ProtocolAPI> sorted = protocols.stream().filter(protocolApi -> protocolApi != null).sorted(new Sorting<>(ProtocolAPI::ordinal)).collect(Collectors.toList());
		if (sorted.size() == 1 || sorted.stream().allMatch(protocolApi -> protocolApi.getProtocolNumber() == sorted.get(sorted.size() - 1).getProtocolNumber()))
			return sorted.get(0).getCompleteName();
		return sorted.get(sorted.size() - 1).getName() + " à " + sorted.get(0).getName();
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static String getName(int protocolNumber) {
		List<ProtocolAPI> protocols = Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).collect(Collectors.toList());
		if (protocols.isEmpty())
			return "unknown (" + protocolNumber + ")";
		String lastVersion = protocols.iterator().next().getName();
		if (protocols.size() == 1)
			return lastVersion;
		Iterator<ProtocolAPI> it = protocols.iterator();
		ProtocolAPI next = null;
		while (it.hasNext())
			next = it.next();
		String firstVersion = next.getName();
		return firstVersion + "-" + lastVersion;
	}

	/**
	 * Get the big version of protocol @param protocolNumber
	 * Prefer using {@link #getAll(int)} to get all ProtocolAPI with a protocolNumber as they are severial ProtocolAPI with the same protocolId
	 */
	@Nullable
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI getHighestVersion(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).findFirst().orElse(null);
	}

	@Nullable
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI getLowerVersion(int protocolNumber) {
		Iterator<ProtocolAPI> it = Arrays.stream(ProtocolAPI.values()).iterator();
		ProtocolAPI protocolApi = null;
		while (it.hasNext()) {
			ProtocolAPI maybeProtocolApi = it.next();
			if (maybeProtocolApi.getProtocolNumber() == protocolNumber && (protocolApi == null || protocolApi.ordinal() > maybeProtocolApi.ordinal()))
				protocolApi = maybeProtocolApi;
		}
		return protocolApi;
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static String getVersion(int protocolNumber) {
		ProtocolAPI protocolAPI = Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).findFirst().orElse(null);
		if (protocolAPI == null)
			return "unknown (" + protocolNumber + ")";
		return protocolAPI.getCompleteName();
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static List<ProtocolAPI> getAll(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).collect(Collectors.toList());
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static List<ProtocolAPI> getAll(List<Integer> protocolsNumbers) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> protocolsNumbers.contains(p.getProtocolNumber())).collect(Collectors.toList());
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static List<ProtocolAPI> getAllUnderI(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() <= protocolNumber).collect(Collectors.toList());
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static List<ProtocolAPI> getAllUnderE(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() < protocolNumber).collect(Collectors.toList());
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static List<ProtocolAPI> getAllUpperI(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() >= protocolNumber).collect(Collectors.toList());
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static List<ProtocolAPI> getAllUpperE(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() > protocolNumber).collect(Collectors.toList());
	}

	@Nullable
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI get(String version) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getName().equals(version)).findFirst().orElse(null);
	}

	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI getByEnumName(String version) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.name().equals(version)).findFirst().orElse(ProtocolAPI.UNKNOW);
	}

	/**
	 * Get big version allowed protocols for Olympa Servers, based on {@link #allow} & {@link #notRecommended}.
	 * Bad use for a server like ZTA, as this server only allows one version and the method is not affected by this change.
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI getLastVersion() {
		return Arrays.stream(ProtocolAPI.values()).filter(protocolApi -> protocolApi.isAllowed()).findFirst().orElse(null);
	}

	/**
	 * Get big version allowed protocols for Olympa Servers, based on {@link #allow} & {@link #notRecommended}.
	 * Bad use for a server like ZTA, as this server only allows one version and the method is not affected by this change.
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT_BUNGEE)
	public static ProtocolAPI getRecommandedVersion() {
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

	public static Entry<String, String> getRangeEntry(List<ProtocolAPI> protocols) {
		String first = "unknown";
		String last = "unknown";
		if (protocols.size() == 1) {
			ProtocolAPI firstUniqueObject = protocols.get(0);
			List<ProtocolAPI> same = firstUniqueObject.getSameProtocol();
			if (same.size() == 1)
				return new AbstractMap.SimpleEntry<>(firstUniqueObject.getName(), firstUniqueObject.getName());
			else
				return getRangeEntry(same);
		}
		Iterator<ProtocolAPI> it = protocols.stream().sorted(new Sorting<>(ProtocolAPI::ordinal)).iterator();
		if (it.hasNext())
			last = it.next().getName();
		while (it.hasNext())
			first = it.next().getName();
		if (first.equals("unknown"))
			first = last;
		return new AbstractMap.SimpleEntry<>(first, last);
	}

	/**
	 * Get default Spigot protocol
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static ProtocolAPI getNativeSpigotProtocolAPI() {
		if (defaultVersion == null)
			defaultVersion = get(getNativeSpigotVersion());
		return defaultVersion;
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static String getNativeSpigotVersion() {
		if (!LinkSpigotBungee.getInstance().isSpigot())
			throw new IllegalAccessError("Can't get BukkitVersion on Bungee instance.");
		if (version == null)
			version = Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf('-'));
		return version;
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static String getVersionSupportedToString() {
		List<String> vers = getNativeVersionsSupported();
		return vers.isEmpty() ? null : ColorUtils.join(vers);
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static String[] getVersionSupportedArray() {
		List<String> vers = getNativeVersionsSupported();
		return new String[] { vers.get(vers.size() - 1), vers.get(0) };
	}

	/**
	 * @throw IllegalAccessError when bungee instance call this method
	 */
	@SpigotOrBungee(allow = AllowedFramework.SPIGOT)
	public static List<String> getNativeVersionsSupported() {
		ProtocolAPI defaultProto = getNativeSpigotProtocolAPI();
		if (defaultProto != null)
			return getAll(defaultProto.getProtocolNumber()).stream().map(ProtocolAPI::getName).collect(Collectors.toList());
		return new ArrayList<>();
	}

	private final int protocolNumber;
	private final boolean allow;
	private final boolean notRecommended;
	private final boolean snapshot;

	ProtocolAPI(int protocolNumber) {
		this(protocolNumber, true, false, false);
	}

	ProtocolAPI(int protocolNumber, boolean allow) {
		this(protocolNumber, allow, false, false);
	}

	ProtocolAPI(int protocolNumber, boolean allow, boolean notRecommended) {
		this(protocolNumber, allow, notRecommended, false);
	}

	ProtocolAPI(int protocolNumber, boolean allow, boolean notRecommended, boolean snapshot) {
		this.protocolNumber = protocolNumber;
		this.allow = allow;
		this.notRecommended = notRecommended;
		this.snapshot = snapshot;
	}

	public boolean contains(int protocolNumber) {
		List<ProtocolAPI> all = getAllVersionOfProtocol();
		return all.stream().anyMatch(protocol -> protocol.getProtocolNumber() == protocolNumber);
	}

	public boolean contains(ProtocolAPI protocol) {
		List<ProtocolAPI> all = getAllVersionOfProtocol();
		return all.contains(protocol);
	}

	public String getName() {
		if (!toString().startsWith("SNAPSHOT"))
			return toString().replace("_", ".").substring(1);
		return Utils.capitalize(toString().replaceFirst("_", " ").replace("_", ".").replace("$", "-"));
	}

	public List<ProtocolAPI> getAllVersionOfProtocol() {
		return getAll(getProtocolNumber());
	}

	public String getCompleteName() {
		List<ProtocolAPI> all = getAllVersionOfProtocol();
		int size = all.size();
		ProtocolAPI latest = all.get(0);
		if (size == 1)
			return latest.getName();
		ProtocolAPI oldest = all.get(size - 1);
		return oldest.getName() + "-" + latest.getName();
	}

	public int getProtocolNumber() {
		return protocolNumber;
	}

	@Nullable
	public List<ProtocolAPI> getSameProtocol() {
		return getAll(protocolNumber);
	}

	@Nullable
	public ProtocolAPI getUpperProtocol() {
		int ordinal = ordinal();
		ProtocolAPI[] values = ProtocolAPI.values();
		if (ordinal - 1 < 0)
			return null;
		return values[ordinal() - 1];
	}

	@Nullable
	public ProtocolAPI getLowerProtocol() {
		int ordinal = ordinal();
		ProtocolAPI[] values = ProtocolAPI.values();
		if (ordinal + 1 > values.length - 1)
			return null;
		return ProtocolAPI.values()[ordinal() + 1];
	}

	public boolean isAllowed() {
		return allow;
	}

	public boolean isNotRecommended() {
		return notRecommended || snapshot;
	}

	public boolean isSnapshot() {
		return snapshot;
	}

}
