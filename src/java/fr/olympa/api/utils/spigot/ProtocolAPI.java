package fr.olympa.api.utils.spigot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;

import fr.olympa.api.chat.ColorUtils;

// https://wiki.vg/Protocol_version_numbers
public enum ProtocolAPI {

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
	V1_8_9(47),
	V1_8_8(47),
	V1_8_7(47),
	V1_8_6(47),
	V1_8_5(47),
	V1_8_4(47),
	V1_8_3(47),
	V1_8_2(47),
	V1_8_1(47),
	V1_8(47),
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
	
	public static ProtocolAPI get(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).findFirst().get();
	}

	public static List<ProtocolAPI> getAll(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).collect(Collectors.toList());
	}

	public static ProtocolAPI get(String version) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getName().equals(version)).findFirst().get();
	}

	public static ProtocolAPI getDefaultProtocol() {
		if (defaultProtocol == null) defaultProtocol = get(getVersion());
		return defaultProtocol;
	}

	public static ProtocolAPI getLastVersion() {
		return Arrays.stream(ProtocolAPI.values()).filter(ProtocolAPI::isAllowed).findFirst().orElse(null);
	}

	public static ProtocolAPI getFirstVersion() {
		return Arrays.stream(ProtocolAPI.values()).filter(ProtocolAPI::isAllowed).reduce((first, second) -> second).orElse(null);
	}

	public static List<ProtocolAPI> gets(int protocolNumber) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getProtocolNumber() == protocolNumber).collect(Collectors.toList());
	}

	public static String getVersion() {
		if (version == null) version = Bukkit.getBukkitVersion().substring(0, Bukkit.getBukkitVersion().indexOf('-'));
		return version;
	}

	public static String getVersionSupportedToString() {
		List<String> vers = getVersionSupported();
		return vers.isEmpty() ? null : ColorUtils.join(vers);
	}

	public static String[] getVersionSupportedArray() {
		List<String> vers = getVersionSupported();
		return new String[] { vers.get(vers.size() - 1), vers.get(0) };
	}

	public static List<String> getVersionSupported() {
		ProtocolAPI defaultProto = getDefaultProtocol();
		if (defaultProto != null)
			return gets(defaultProto.getProtocolNumber()).stream().map(ProtocolAPI::getName).collect(Collectors.toList());
		return new ArrayList<>();
	}

	private final int protocolNumber;
	private boolean allow = true;

	private ProtocolAPI(int protocolNumber) {
		this.protocolNumber = protocolNumber;
	}

	private ProtocolAPI(int versionNumber, boolean allow) {
		protocolNumber = versionNumber;
		this.allow = allow;
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

	public boolean isSupported() {
		ProtocolAPI defaultProto = getDefaultProtocol();
		return defaultProto != null && getProtocolNumber() <= defaultProto.getProtocolNumber();
	}
}
