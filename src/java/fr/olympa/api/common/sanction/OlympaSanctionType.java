package fr.olympa.api.common.sanction;

import java.util.Arrays;

public enum OlympaSanctionType {

	BAN(1, "Ban"),
	BANIP(2, "BanIP"),
	MUTE(3, "Mute"),
	MUTEIP(4, "Mute"),
	KICK(5, "Kick");

	private static final String TEMP = "Temp";

	public static OlympaSanctionType getByID(int i) {
		return Arrays.stream(OlympaSanctionType.values()).filter(id -> id.getId() == i).findFirst().orElse(null);
	}

	int id;
	String s;

	OlympaSanctionType(int id, String s) {
		this.id = id;
		this.s = s;
	}

	public int getId() {
		return id;
	}

	public boolean isBanType() {
		return id == BAN.id || id == BANIP.id;
	}

	public boolean isMuteType() {
		return id == MUTE.id || id == MUTEIP.id;
	}

	public boolean isKickType() {
		return id == KICK.id;
	}

	public String getName() {
		return s;
	}

	public String getName(boolean isTemp) {
		return isTemp && this != KICK ? TEMP + s : s;
	}

	public String getNameForPlayer() {
		if (this == BANIP)
			return BAN.s;
		return s;
	}

	public String getNameForPlayer(boolean isTemp) {
		return isTemp ? TEMP + getNameForPlayer() : getNameForPlayer();
	}
}
