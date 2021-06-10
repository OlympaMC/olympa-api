package fr.olympa.api.spigot.region.tracking;

public enum ActionResult {
	
	ALLOW, DENY, TELEPORT_ELSEWHERE;

	public ActionResult or(ActionResult other) {
		if (this == ALLOW) return other;
		if (this == DENY && other != ALLOW) return other;
		return TELEPORT_ELSEWHERE;
	}
	
}
