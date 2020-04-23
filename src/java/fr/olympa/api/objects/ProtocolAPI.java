package fr.olympa.api.objects;

import java.util.Arrays;

public enum ProtocolAPI {

	V1_16(713, false),
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
	V1_11_1(316),
	V1_11(315),
	V1_10(210),
	V1_9_3(110),
	V1_9_2(109),
	V1_9_1(108),
	V1_9(107),
	V1_8(47),
	V1_7_10(5, false),
	V1_7(4, false),
	V1_6_4(78, false),
	V1_6_2(74, false),
	V1_6_1(73, false),
	V1_5_2(61, false),
	V1_5_1(60, false),
	V1_4_7(51, false),
	;

	public static ProtocolAPI get(int version) {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.getVersionNumber() == version).findFirst().orElse(null);
	}

	public static ProtocolAPI getLastVersion() {
		return Arrays.stream(ProtocolAPI.values()).filter(p -> p.isAllow()).findFirst().orElse(null);
	}

	final private int versionNumber;

	private boolean allow = true;

	private ProtocolAPI(int versionNumber) {
		this.versionNumber = versionNumber;
	}

	private ProtocolAPI(int versionNumber, boolean allow) {
		this.versionNumber = versionNumber;
		this.allow = allow;
	}

	public String getName() {
		return toString().replaceAll("_", ".").substring(1);
	}

	public int getVersionNumber() {
		return versionNumber;
	}

	public boolean isAllow() {
		return allow;
	}
}
